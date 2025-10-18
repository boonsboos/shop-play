package nl.connectplay.scoreplay.models.dto.score

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.utilities.UUIDSerializer
import nl.connectplay.scoreplay.utilities.LocalDateTimeSerializer
import java.util.UUID
import java.time.LocalDateTime

@Serializable
data class ScoreDto(
    @Serializable(with = UUIDSerializer::class) val scoreId: UUID,
    val score: Double,
    @Serializable(with = LocalDateTimeSerializer::class) val achievedOn: LocalDateTime,
    val turn: Int,
    @Serializable(with = UUIDSerializer::class) val sessionId: UUID,
    @Serializable(with = UUIDSerializer::class) val sessionPlayerId: UUID,
    val gameId: Int
)