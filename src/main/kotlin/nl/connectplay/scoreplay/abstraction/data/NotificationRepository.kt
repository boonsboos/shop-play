package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto

interface NotificationRepository {
    suspend fun saveNotificationAsync(notification: NewNotificationDto)
}