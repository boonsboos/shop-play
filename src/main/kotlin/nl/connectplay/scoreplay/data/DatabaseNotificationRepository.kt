package nl.connectplay.scoreplay.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto

class DatabaseNotificationRepository(private val database: Database) : NotificationRepository {

    val insertNotificationSql = """
        INSERT INTO notifications (user_id, content)
        VALUES (?, ?)
    """.trimIndent()

    override suspend fun saveNotificationAsync(notification: NewNotificationDto): Boolean = withContext(Dispatchers.Default) {
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

}