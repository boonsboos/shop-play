package nl.connectplay.scoreplay.data

import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.dto.notifications.NotificationDto
import kotlinx.coroutines.async
import java.util.UUID

class DatabaseNotificationRepository(private val database: Database) : NotificationRepository {
    override suspend fun saveNotificationAsync(notification: NewNotificationDto) {
        TODO("Not yet implemented")
    }

    override suspend fun getNotificationByIdAsync(notificationId: UUID, userId: Int): NotificationDto? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """SELECT
                                notification_id,
                                content,
                                read
                             FROM notifications
                                score_id = ?
                          """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setString(1, notificationId.toString())

                val resultSet = stmt?.executeQuery()
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

    override suspend fun getAllNotificationsAsync(limit: Int?, offset: Int? ,userId: Int): List<NotificationDto>? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val notifications = mutableListOf<NotificationDto>()

                    val sql = """
                        SELECT
                              notification_id,
                              content,
                              read
                        FROM notifications
                        WHERE user_id = ?
                        LIMIT ? OFFSET ?""".trimIndent()

                    val statement = connection.prepareStatement(sql)
                    statement.setInt(1, limit ?: 25)
                    statement.setInt(2, offset ?: 0)
                    val resultSet = statement.executeQuery()

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
                    val statement = connection.prepareStatement("DELETE FROM notifications WHERE notification_id = ?")
                    statement.setString(1, notificationId.toString())

                    val notificationDeleted = statement.executeUpdate()
                    statement.close()
                    notificationDeleted == 1
                } ?: false
            }.await()
        }
    }

    override suspend fun setNotificationAsReadAsync(notificationId: UUID, userID: Int): Boolean {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val sql = "UPDATE notifications SET read = b'1' WHERE notification_id = ?"
                    val statement = connection.prepareStatement(sql)
                    statement.setString(1, notificationId.toString())

                    val affectedRows = statement.executeUpdate()
                    statement.close()

                    affectedRows > 0
                } ?: false
            }.await()
        }
    }
}





