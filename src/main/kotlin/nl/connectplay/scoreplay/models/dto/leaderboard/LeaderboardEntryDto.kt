package nl.connectplay.scoreplay.models.dto.leaderboard

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryDto(val playerName: String, val score: Int, val achievedAt: String? = null) // add null so it is optional