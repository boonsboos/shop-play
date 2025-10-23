package nl.connectplay.scoreplay.models.dto.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class UpdateGameDto(
    val name: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val duration: Int? = null,
    val minAge: Int? = null,
    val releaseDate: LocalDate? = null,
    val scoringMethod: Int? = null
)
