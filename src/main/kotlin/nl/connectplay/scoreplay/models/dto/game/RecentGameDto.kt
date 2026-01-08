package nl.connectplay.scoreplay.models.dto.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class RecentGameDto(
        val id: Int,
        val scoringMethodId: Int,
        val name: String,
        val description: String,
        val publisher: String,
        val minPlayers: Int?,
        val maxPlayers: Int?,
        val duration: Int?,
        val minAge: Int?,
        val releaseDate: LocalDate?,
        val lastPlayed: LocalDate
)