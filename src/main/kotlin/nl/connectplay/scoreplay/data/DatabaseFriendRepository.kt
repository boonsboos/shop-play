package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.FriendRepository

class DatabaseFriendRepository(private val database: Database) : FriendRepository {

    private val addFriendSql = """
        INSERT INTO friends (user_id, friend_id)
        VALUES (?, ?);
    """.trimIndent()

    override suspend fun addFriend(userId: Int, friendId: Int): Boolean = coroutineScope{
        async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(addFriendSql)
                statement.setInt(1, userId)
                statement.setInt(2, friendId)

                statement.execute()
            }
        }.await() ?: false // we can return default false here because something went wrong
    }

    private val deleteFriendSql = """
        DELETE FROM friends
        WHERE user_id = ? AND friend_id = ?;
    """.trimIndent()

    override suspend fun deleteFriend(userId: Int, friendId: Int): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(deleteFriendSql)
                statement.setInt(1, userId)
                statement.setInt(2, friendId)

                val result = statement.execute()

                statement.close()

                result
            }
        }.await() ?: false // we can return default false here because something went wrong
    }

    private val getFriendsSql = """
        SELECT friend_id FROM friends
        WHERE user_id = ?;
    """.trimIndent()

    override suspend fun getFriends(userId: Int): List<Int>? = coroutineScope {
        async {
            // autoclose connection after leaving scope
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getFriendsSql)
                statement.setInt(1, userId)

                val resultSet = statement.executeQuery()

                val friendIds = mutableListOf<Int>()
                // get all friend ids
                while(!resultSet.last()) {
                    friendIds.add(
                        resultSet.getInt("friend_id")
                    )
                }

                // close open resources
                resultSet.close()
                statement.close()

                friendIds.toList()
            }
        }.await()
    }
}