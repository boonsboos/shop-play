package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDate

/**
 * This model represents a game entity from the database.
 * It contains the basic information about a game.
 */
open class Game(
    val id: Int,
    val name: String,
    val description: String,
    val publisher: String,
    var minPlayers: Int?,
    var maxPlayers: Int?,
    var duration: Int?,
    var minAge: Int?,
    var releaseDate: LocalDate?) {
}