package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.game.GameDto
import nl.connectplay.scoreplay.models.dto.game.RecentGameDto

/**
 * This model represents a game entity from the database.
 * It contains the basic information about a game.
 */
@Serializable
data class Game(
    val id: Int,
    val scoringMethodId: Int,
    val name: String,
    val description: String,
    val publisher: String,
    var minPlayers: Int?,
    var maxPlayers: Int?,
    var duration: Int?,
    var minAge: Int?,
    var releaseDate: LocalDate?
) {
    fun withPictures(pictures: List<String>): GameDto =
        GameDto(
            id = id,
            scoringMethodId = scoringMethodId,
            name = name,
            description = description,
            publisher = publisher,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            duration = duration,
            minAge = minAge,
            releaseDate = releaseDate,
            pictures = pictures
        )

    fun withLastPlayed(lastPlayed: LocalDate): RecentGameDto =
        RecentGameDto(
            id = id,
            scoringMethodId = scoringMethodId,
            name = name,
            description = description,
            publisher = publisher,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            duration = duration,
            minAge = minAge,
            releaseDate = releaseDate,
            lastPlayed = lastPlayed
        )
}