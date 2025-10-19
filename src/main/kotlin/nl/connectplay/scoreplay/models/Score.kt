package nl.connectplay.scoreplay.models

import java.time.LocalDateTime
import java.util.UUID


data class Score(
    val scoreId: UUID,
    val sessionId: UUID,
    val sessionPlayerId: UUID,
    val gameId: Int,
    val score: Double,
    val turn: Int,
    val achievedOn: LocalDateTime
)
