package nl.connectplay.scoreplay.data

import io.ktor.http.parameters
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.UserDto
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import kotlin.coroutines.coroutineContext

class DatabaseUserRepository(private val database: Database) : UserRepository {

    /**
     * Asynchronously retrieves a list of users from the database.
     *
     * @param limit the maximum number of users to return
     * @param offset the number of users to skip before starting to collect results
     * @param query optional search string to filter users by username
     * @return a list of [UserDto] objects matching the criteria, or null if no users are found
     * @throws java.sql.SQLException if a database error occurs
     */
    override suspend fun getUsersAsync(
        limit: Int?, offset: Int?, query: String?
    ): List<UserDto>? {
        // coroutineScope ensures that any child coroutine (like async)
        // will complete before this function returns, and exceptions are properly propagated.
        return coroutineScope {
            // async launches the database operation in a separate coroutine,
            // allowing for potential parallelism with other async tasks (if any).
            async {
                // `use` ensures the connection is automatically closed after the block,
                // even if an exception occurs.
                database.connection?.use { connection ->
                    var sql = """
                        SELECT u.user_name, p.picture_url FROM users AS u
                        LEFT JOIN pictures AS p on u.profile_picture = p.picture_id
                        WHERE u.user_name LIKE ?
                        LIMIT ? OFFSET ?
                    """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setString(1, "%${query ?: ""}%")
                    stmt.setInt(2, limit ?: 25)
                    stmt.setInt(3, offset ?: 0)

                    val resultSet = stmt?.executeQuery()

                    val users = mutableListOf<UserDto>()

                    while (resultSet?.next() == true) {
                        val user = UserDto(
                            username = resultSet.getString("user_name"),
                            profilePicture = resultSet.getString("picture_url"),
                        )
                        users.add(user)
                    }

                    // Always close JDBC resources explicitly (though .use would handle the connection).
                    stmt?.close()
                    resultSet?.close()

                    // Return the list (converted to an immutable list for safety).
                    users.toList()
                }
            }.await()
        }
    }

    /**
     * Asynchronously retrieves a single user from the database by their ID.
     *
     * @param userId the unique ID of the user to retrieve
     * @return a [UserDto] object matching the ID, or null if no user is found
     * @throws java.sql.SQLException if a database error occurs
     */
    override suspend fun getUserByIdAsync(userId: Int): UserDto? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    var sql = """
                        SELECT u.user_name, p.picture_url FROM users AS u
                        LEFT JOIN pictures AS p on u.profile_picture = p.picture_id
                        WHERE u.user_id = ?
                    """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setInt(1, userId)

                    val resultSet = stmt?.executeQuery()
                    var user: UserDto? = null;
                    if (resultSet?.next() == true) {
                        user = UserDto(
                            username = resultSet.getString("user_name"),
                            profilePicture = resultSet.getString("picture_url"),
                        )
                    }

                    stmt?.close()
                    resultSet?.close()

                    user
                }
            }.await()
        }
    }

    val getUserByNameOrEmailSql = """
        SELECT user_id, user_name, email, password_hash, profile_picture
        FROM users
        WHERE user_name = ? OR email = ?
    """.trimIndent()

    override suspend fun getUserByNameOrEmail(username: String?, email: String?): User? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getUserByNameOrEmailSql)
                statement.setString(1, username)
                statement.setString(2, email)

                val resultSet = statement.executeQuery()
                var user: User? = null
                if (resultSet.next()) {
                    user = User(
                        resultSet.getInt("user_id"),
                        resultSet.getString("user_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("profile_picture")?.let { UUID.fromString(it) }
                    )
                }

                resultSet.close()
                statement.close()

                user
            }
        }.await()
    }

    override suspend fun addUser(user: CreateUserDto) {
        return coroutineScope {
            async {
                database.connection?.use { connection -> // open the connection to the database
                    val sql =
                        "INSERT INTO users (user_name, email, password_hash) VALUES (?, ?, ?)" // sql with placeholders to prevent SQL injection
                    val stmt = connection.prepareStatement(sql)

                    stmt.setString(1, user.username)
                    stmt.setString(2, user.email)
                    // BCrypt hashed the password and extra text to password with gensalt()
                    stmt.setString(3, BCrypt.hashpw(user.password, BCrypt.gensalt()))
                    stmt.executeUpdate() // execute the sql insert command
                    stmt.close()
                }
            }.await()
        }
    }
}