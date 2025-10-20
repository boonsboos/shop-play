package nl.connectplay.scoreplay.abstraction.data


import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.dto.notifications.NotificationDto
import java.util.UUID


interface NotificationRepository {
    suspend fun saveNotificationAsync(notification: NewNotificationDto)
    suspend fun getNotificationByUserAsync(limit: Int? = 25, offset: Int? = 0, userId: Int? = null): List<NotificationDto>?
    suspend fun deleteNotificationAsync(notificationId: UUID): Boolean
    suspend fun setNotificationAsReadAsync(notificationId: UUID): Boolean
}
