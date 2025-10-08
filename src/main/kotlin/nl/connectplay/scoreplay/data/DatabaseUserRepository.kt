package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.dto.UserDto
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import org.mindrot.jbcrypt.BCrypt

class DatabaseUserRepository : UserRepository {
    private val database = Database()

    override suspend fun getUsersAsync(
        limit: Int?, offset: Int?, query: String?
    ): List<UserDto>? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val users = mutableListOf<UserDto>()

                    var sql = """
                        SELECT u.user_name, p.picture_url FROM users AS u
                        JOIN pictures AS p on u.profile_picture = p.picture_id
                        WHERE u.user_name LIKE ?
                        LIMIT ? OFFSET ?
                    """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setString(1, "%${query ?: ""}%")
                    stmt.setInt(2,limit ?: 25)
                    stmt.setInt(3,offset ?: 0)

                    val resultSet = stmt?.executeQuery()

                    while (resultSet?.next() == true) {
                        val user = UserDto(
                            username = resultSet.getString("user_name"),
                            profilePicture = resultSet.getString("picture_url"),
                        )
                        users.add(user)
                    }

                    stmt?.close()
                    resultSet?.close()

                    users.toList()
                }
            }.await()
        }
    }

    override suspend fun getUserByIdAsync(userId: Int): UserDto? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    var sql = """
                        SELECT u.user_name, p.picture_url FROM users AS u
                        JOIN pictures AS p on u.profile_picture = p.picture_id
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

    override suspend fun addUser(user: CreateUserDto) {
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
    }
}