package nl.connectplay.scoreplay.models.dto.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CreateGameDto(
    val name: String,
    val description: String,
    val publisher: String,
    val scoringMethod: Int = 1,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val duration: Int? = null,
    val minAge: Int? = null,
    val releaseDate: LocalDate? = null
)
