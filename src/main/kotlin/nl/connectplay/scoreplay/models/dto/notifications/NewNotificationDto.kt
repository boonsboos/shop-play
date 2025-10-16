package nl.connectplay.scoreplay.models.dto.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NewNotificationDto(val userId: Int, val notification: BaseEvent)