package nl.connectplay.scoreplay.models.dto

import kotlinx.datetime.LocalDate
import nl.connectplay.scoreplay.models.Game

class GameWithDetailsDto(
    id: Int,
    name: String,
    description: String,
    publisher: String,
    minPlayers: Int?,
    maxPlayers: Int?,
    duration: Int?,
    minAge: Int?,
    releaseDate: LocalDate?,
) : Game(id, name, description, publisher, minPlayers, maxPlayers, duration, minAge, releaseDate) {
}