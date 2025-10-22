package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.dto.notifications.NotificationDto
import java.util.UUID

interface NotificationRepository {
    suspend fun saveNotificationAsync(notification: NewNotificationDto)
    suspend fun getNotificationByIdAsync(notificationId: UUID, userId: Int): NotificationDto?
    suspend fun getAllNotificationsAsync(limit: Int?, offset: Int?, userId: Int): List<NotificationDto>?
    suspend fun deleteNotificationAsync(notificationId: UUID, userID: Int): Boolean
    suspend fun setNotificationAsReadAsync(notificationId: UUID, userID: Int): Boolean
}
