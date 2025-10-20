package nl.connectplay.scoreplay.models.dto

import kotlinx.datetime.LocalDate

data class CreateGameDto(
    val name: String,
    val description: String,
    val publisher: String,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val duration: Int? = null,
    val minAge: Int? = null,
    val releaseDate: LocalDate? = null
)
