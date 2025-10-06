package nl.connectplay.scoreplay.models.dto.session

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.utilities.UUIDSerializer
import java.util.UUID

@Serializable
data class SessionIdDto(
    // needs to be on this level
    @Serializable(with = UUIDSerializer::class) val sessionId: UUID
)