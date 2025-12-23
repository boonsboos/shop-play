package nl.connectplay.scoreplay.models.dto.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.leaderboard.LeaderboardEntryDto

@Serializable
data class GameDto(
    val id: Int,
    val scoringMethodId: Int = 1, // defaults to "Highest score wins"
    val name: String,
    val description: String,
    val publisher: String,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val duration: Int? = null,
    val minAge: Int? = null,
    val releaseDate: LocalDate? = null,
    val pictures: List<String> = listOf(),
) {
    fun withPodium(podium: List<LeaderboardEntryDto>) =
        FollowedGameDto(
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
            pictures = pictures,
            podium = podium
        )

    fun withFollowing(isFollowing: Boolean) =
        GameDetailDto(
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
            pictures = pictures,
            following = isFollowing
        )
}
