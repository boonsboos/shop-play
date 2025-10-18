package nl.connectplay.scoreplay.models.dto.score

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.utilities.LocalDateTimeSerializer
import nl.connectplay.scoreplay.utilities.UUIDSerializer
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class UpdateScoreDto(
    @Serializable(with = UUIDSerializer::class) val sessionId: UUID,
    @Serializable(with = UUIDSerializer::class) val sessionPlayerId: UUID,
    @Serializable(with = UUIDSerializer::class) val scoreId: UUID,
    val userId: Int,
    val gameId: Int,
    val score: Double,
    val turn: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val achievedOn: LocalDateTime?,
)