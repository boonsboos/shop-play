package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.dto.UserDto

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
}