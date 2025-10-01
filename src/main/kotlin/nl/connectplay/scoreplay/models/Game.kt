package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDate

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