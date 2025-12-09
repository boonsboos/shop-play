package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.FriendRepository
import java.sql.PreparedStatement
import java.sql.SQLIntegrityConstraintViolationException

class DatabaseFriendRepository(private val database: Database) : FriendRepository {

    private val addFriendSql = """
        INSERT INTO friends (user_id, friend_id)
        VALUES (?, ?);
    """.trimIndent()

    override suspend fun addFriendAsync(userId: Int, friendId: Int): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val statement = connection.prepareStatement(addFriendSql)
                    statement.setInt(1, userId)
                    statement.setInt(2, friendId)
                    val affectedRows = statement.executeUpdate()
                    statement.close()
                    return@async affectedRows > 0
                } catch (e: SQLIntegrityConstraintViolationException) {
                    return@async false // request already send
                }
            }
        }.await() ?: false // we can return default false here because something went wrong
    }

    private val deleteFriendSql = """
        DELETE FROM friends
        WHERE user_id = ? AND friend_id = ?;
    """.trimIndent()

    override suspend fun deleteFriendAsync(userId: Int, friendId: Int): Boolean = coroutineScope {
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
       SELECT DISTINCT(f1.friend_id)
        FROM friends f1
        JOIN friends f2 ON f1.user_id = f2.friend_id AND f1.friend_id = f2.user_id
        WHERE f1.friend_id <> ?
        AND f1.user_id = ?;
    """.trimIndent()

    override suspend fun getFriendsAsync(userId: Int): List<Int>? = coroutineScope {
        async {
            // autoclose connection after leaving scope
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getFriendsSql)
                statement.setInt(1, userId)
                statement.setInt(2, userId)

                executeGetFriendIdsQuery(statement)
            }
        }.await()
    }

    private val getOutstandingFriendRequestsSql = """
        SELECT friend_id
        FROM friends
        WHERE user_id = ?;
    """.trimIndent()

    override suspend fun getOutstandingFriendRequestsAsync(userId: Int): List<Int>? = coroutineScope {
        async {
            // autoclose connection after leaving scope
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getOutstandingFriendRequestsSql)
                statement.setInt(1, userId)

                executeGetFriendIdsQuery(statement)
            }
        }.await()
    }

    private val getPendingFriendRequestsSql = """
        SELECT user_id
        FROM friends
        WHERE friend_id = ?;
    """.trimIndent()

    override suspend fun getPendingFriendRequestsAsync(userId: Int): List<Int>? = coroutineScope {
        async {
            // autoclose connection after leaving scope
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getPendingFriendRequestsSql)
                statement.setInt(1, userId)

                val resultSet = statement.executeQuery()

                val friendIds = mutableListOf<Int>()

                // get all friend ids
                while (resultSet.next()) {
                    friendIds.add(
                        resultSet.getInt("user_id")
                    )
                }

                // close open resources
                resultSet.close()
                statement.close()

                friendIds.toList()
            }
        }.await()
    }

    private val getFriendsWithOffsetSql = """
        SELECT DISTINCT(f1.friend_id)
        FROM friends f1
        JOIN friends f2 ON f1.user_id = f2.friend_id AND f1.friend_id = f2.user_id
        WHERE f1.friend_id <> ?
        AND f1.user_id = ?
        LIMIT ? OFFSET ?;
    """.trimIndent()

    override suspend fun getFriendsAsync(userId: Int, limit: Int, offset: Int): List<Int>? = coroutineScope {
        async {
            // autoclose connection after leaving scope
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getFriendsWithOffsetSql)
                statement.setInt(1, userId)
                statement.setInt(2, userId)
                statement.setInt(3, limit)
                statement.setInt(4, offset)

                executeGetFriendIdsQuery(statement)
            }
        }.await()
    }

    /**
     * Utility function to reduce code duplication in [getFriendsAsync].
     * This should only ever be called from a coroutine.
     */
    private suspend fun executeGetFriendIdsQuery(statement: PreparedStatement): List<Int> {
        val resultSet = statement.executeQuery()

        val friendIds = mutableListOf<Int>()

        // get all friend ids
        while (resultSet.next()) {
            friendIds.add(
                resultSet.getInt("friend_id")
            )
        }

        // close open resources
        resultSet.close()
        statement.close()

        return friendIds.toList()
    }
}