package nl.connectplay.scoreplay.models.dto.notifications

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.events.BaseEvent

@Serializable
data class NewNotificationDto(val userId: Int, val notification: BaseEvent)