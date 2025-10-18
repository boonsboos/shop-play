package nl.connectplay.scoreplay.models.dto

import kotlinx.datetime.LocalDate
import nl.connectplay.scoreplay.models.Game

data class GameDto(
    val id: Int,
    val scoringMethodId: Int? = null,
    val name: String,
    val description: String,
    val publisher: String,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val duration: Int? = null,
    val minAge: Int? = null,
    val releaseDate: LocalDate? = null
)

// simple mapper
fun Game.toDto() = GameDto(
    id, null, name, description, publisher, minPlayers, maxPlayers, duration, minAge, releaseDate
)
