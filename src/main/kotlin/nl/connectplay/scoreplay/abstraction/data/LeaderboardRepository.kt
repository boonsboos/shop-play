package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.dto.leaderboard.LeaderboardEntryDto

interface LeaderboardRepository {
    /**
     * Fetch the top 100 scores for game
     * @param gameId the id of the game
     * @return [List] of [LeaderboardEntryDto] representing the leaderboard entries
     */
    suspend fun getTopScoresForGame(gameId: Int): List<LeaderboardEntryDto>
}