package nl.connectplay.scoreplay.models.dto.leaderboard

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryDto(val playerName: String, val score: Double, val achievedAt: LocalDateTime)