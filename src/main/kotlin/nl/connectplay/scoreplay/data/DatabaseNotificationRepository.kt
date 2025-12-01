package nl.connectplay.scoreplay.data

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.dto.notifications.NotificationDto
import kotlinx.coroutines.async
import java.util.UUID

class DatabaseNotificationRepository(private val database: Database) : NotificationRepository {

    val insertNotificationSql = """
        INSERT INTO notifications (user_id, content)
        VALUES (?, ?)
    """.trimIndent()

    override suspend fun saveNotificationAsync(notification: NewNotificationDto): Boolean =
        withContext(Dispatchers.Default) {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(insertNotificationSql).apply {
                    setInt(1, notification.userId)
                    setString(2, Json.encodeToString(notification.notification))
                }

                val result = statement.execute()
                statement.close()
                result
            } ?: false
        }

    override suspend fun getNotificationByIdAsync(notificationId: UUID, userId: Int): NotificationDto? =
        coroutineScope {
            async {
                database.connection?.use { connection ->
                    val sql = """SELECT
                                notification_id,
                                content,
                                read
                             FROM notifications
                             WHERE notification_id = ?
                          """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setString(1, notificationId.toString())

                    val resultSet = stmt.executeQuery()
                    var notification: NotificationDto? = null;
                    if (resultSet?.next() == true) {
                        notification = NotificationDto(
                            notificationId = UUID.fromString(resultSet.getString("notification_id")),
                            content = resultSet.getString("content"),
                            read = resultSet.getBoolean("read")
                        )
                    }

                    stmt?.close()
                    resultSet?.close()

                    notification
                }
            }.await()
        }

    override suspend fun getAllNotificationsAsync(userId: Int, limit: Int, offset: Int): List<NotificationDto>? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val notifications = mutableListOf<NotificationDto>()

                    val sql = """
                        SELECT `notification_id`, `user_id`, `content`, `read`
                        FROM notifications
                        WHERE user_id = ?
                        LIMIT ? OFFSET ?
                    """.trimIndent()

                    val statement = connection.prepareStatement(sql)
                    statement.setInt(1, userId)
                    statement.setInt(2, limit)
                    statement.setInt(3, offset)
                    val resultSet = statement.executeQuery()

                    println(resultSet.statement)

                    while (resultSet?.next() == true) {
                        val notification = NotificationDto(
                            notificationId = UUID.fromString(resultSet.getString("notification_id")),
                            content = resultSet.getString("content"),
                            read = resultSet.getBoolean("read"),
                        )
                        notifications.add(notification)
                    }

                    resultSet.close()
                    statement.close()

                    return@async notifications.toList()
                }
            }.await()
        }
    }


    override suspend fun deleteNotificationAsync(notificationId: UUID, userId: Int): Boolean {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val statement =
                        connection.prepareStatement("DELETE FROM notifications WHERE notification_id = ? AND user_id = ?")
                    statement.setString(1, notificationId.toString())
                    statement.setInt(2, userId)

                    val notificationDeleted = statement.executeUpdate()
                    statement.close()
                    notificationDeleted == 1
                } ?: false
            }.await()
        }
    }

    override suspend fun setNotificationAsReadAsync(notificationId: UUID, userId: Int): Boolean {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val sql = "UPDATE notifications SET read = b'1' WHERE notification_id = ? AND user_id = ?"
                    val statement = connection.prepareStatement(sql)
                    statement.setString(1, notificationId.toString())
                    statement.setInt(2, userId)

                    val affectedRows = statement.executeUpdate()
                    statement.close()

                    affectedRows > 0
                } ?: false
            }.await()
        }
    }
}





