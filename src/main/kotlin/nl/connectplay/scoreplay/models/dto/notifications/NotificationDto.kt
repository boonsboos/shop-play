package nl.connectplay.scoreplay.models.dto.notifications

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.utilities.UUIDSerializer
import java.util.UUID

@Serializable
data class NotificationDto(
    @Serializable(with = UUIDSerializer::class) val notificationId: UUID,
    val content: String,
    val read: Boolean,
)