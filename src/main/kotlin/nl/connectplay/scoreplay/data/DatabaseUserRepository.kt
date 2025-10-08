package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.dto.UserDto
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import org.mindrot.jbcrypt.BCrypt
import java.sql.SQLException // to handel the database errors

class DatabaseUserRepository(private val database: Database) : UserRepository {
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
                    """.trimIndent()

                    if (limit != null) {
                        sql += " LIMIT $limit"
                    }

                    if (offset != null) {
                        sql += " OFFSET=$offset"
                    }

//                    if(query != null) {
//                        sql += " LIKE
//                    }

                    println(sql)

                    val statement =
                        connection.prepareStatement(sql)
                    val resultSet = statement?.executeQuery()

                    while (resultSet?.next() == true) {
                        val user = UserDto(
                            username = resultSet.getString("user_name"),
                            profilePicture = resultSet.getString("picture_url"),
                        )
                        users.add(user)
                    }

                    resultSet?.close()
                    statement?.close()

                    users.toList()
                }
            }.await()
        }
    }

    override suspend fun getUserByIdAsync(userId: String): UserDto? {
        TODO("TODO")
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