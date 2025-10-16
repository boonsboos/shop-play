package nl.connectplay.scoreplay.data

import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto

class DatabaseNotificationRepository(private val database: Database) : NotificationRepository {
    override suspend fun saveNotificationAsync(notification: NewNotificationDto) {
        TODO("Not yet implemented")
    }

}