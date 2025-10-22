package nl.connectplay.scoreplay.models.dto.score

import kotlinx.serialization.Serializable

@Serializable
data class UpdateScoreDto(
    val score: Double,
    val turn: Int,
)