package nl.connectplay.scoreplay.models.dto.game

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class GameDetailDto(
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
    val following: Boolean = false
)