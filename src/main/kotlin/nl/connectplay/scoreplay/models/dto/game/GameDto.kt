package nl.connectplay.scoreplay.models.dto

import kotlinx.datetime.LocalDate
import nl.connectplay.scoreplay.models.Game

data class GameDto(
    val id: Int,
    val name: String,
    val description: String,
    val publisher: String,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val duration: Int?,
    val minAge: Int?,
    val releaseDate: LocalDate?
)

// simpele mapper
fun Game.toDto() = GameDto(
    id, name, description, publisher, minPlayers, maxPlayers, duration, minAge, releaseDate
)
