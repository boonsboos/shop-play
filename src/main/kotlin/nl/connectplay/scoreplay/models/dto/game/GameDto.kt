package nl.connectplay.scoreplay.models.dto.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.Game

@Serializable
data class GameDto(
    val id: Int,
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
    id, name, description, publisher, minPlayers, maxPlayers, duration, minAge, releaseDate
)
