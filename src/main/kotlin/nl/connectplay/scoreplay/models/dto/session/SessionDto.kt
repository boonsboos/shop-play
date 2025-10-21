package nl.connectplay.scoreplay.models.dto.session

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.SessionVisibility
import nl.connectplay.scoreplay.utilities.UUIDSerializer
import java.util.*

@Serializable
data class SessionDto(
    @Serializable(with = UUIDSerializer::class) val sessionId: UUID,
    val gameId: Int,
    val hostId: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val endOfSessionPictureUrl: String?,
    val visibility: SessionVisibility
)