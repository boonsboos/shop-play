package nl.connectplay.scoreplay.models.dto.notifications

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.events.BaseEvent
import nl.connectplay.scoreplay.utilities.UUIDSerializer
import java.util.UUID

@Serializable
data class NotificationDto(
    @Serializable(with = UUIDSerializer::class) val notificationId: UUID,
    val userId: Int,
    val content: String,
    val read: Boolean,
)