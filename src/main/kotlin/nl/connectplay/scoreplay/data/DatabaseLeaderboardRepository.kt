package nl.connectplay.scoreplay.data

import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.LeaderboardRepository
import nl.connectplay.scoreplay.models.dto.leaderboard.LeaderboardEntryDto

class DatabaseLeaderboardRepository(private val database: Database) : LeaderboardRepository {
    override suspend fun getTopScoresForGame(gameId: Int): List<LeaderboardEntryDto> = coroutineScope {
        database.connection?.use { connection ->
            val sql = """
                SELECT COALESCE(session_players.guest_name, users.user_name) AS playersName,
                    scores.score,
                    scores.achieved_on,
                    sessions.session_visibility,
                    sessions.host_user_id,
                    session_players.user_id,
                    session_players.session_player_id
                FROM scores
                JOIN sessions ON scores.session_id = sessions.session_id
                JOIN session_players ON scores.session_player_id = session_players.session_player_id
                LEFT JOIN users ON session_players.user_id = users.user_id
                WHERE scores.game_id = ?
                AND sessions.session_visibility IN (2, 3)
                ORDER BY scores.score DESC, scores.achieved_on ASC
                LIMIT 100              
            """.trimIndent()

            val stmt = connection.prepareStatement(sql)
            stmt.setInt(1, gameId)

            // gives a object of the results
            val resultSet = stmt.executeQuery()
            val leaderboardEntries = mutableListOf<LeaderboardEntryDto>()

            while (resultSet.next()) {
                // these fields are the base for the leaderboardentety
                val playerName = resultSet.getString("playersName")
                val score = resultSet.getDouble("score")
                val achievedOn = resultSet.getString("achieved_on")

                // these fields are used to check if de playersname is visable ore not
                val sessionVisibility = resultSet.getInt("session_visibility")
                val hostUserId = resultSet.getInt("host_user_id")
                val userId = resultSet.getInt("user_id")

                // Apply anonymization: hide player names if the session is anonymised and the user is not the host.
                val finalName = if (sessionVisibility == 2 && userId != hostUserId) {
                    "Anonieme speler"
                } else {
                    playerName
                }

                leaderboardEntries.add(
                    LeaderboardEntryDto(
                        playerName = finalName,
                        score = score,
                        achievedAt = achievedOn
                    )
                )
            }
            return@use leaderboardEntries
        } ?: emptyList()
    }
}