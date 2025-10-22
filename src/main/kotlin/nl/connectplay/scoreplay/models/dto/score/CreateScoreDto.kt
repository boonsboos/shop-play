package nl.connectplay.scoreplay.models.dto.score

import kotlinx.serialization.Serializable

@Serializable
data class CreateScoreDto(
    val score: Double,
    val turn: Int,
    val sessionPlayer: SessionPlayerDto,
)