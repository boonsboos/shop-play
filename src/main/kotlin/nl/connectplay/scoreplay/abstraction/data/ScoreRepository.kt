package nl.connectplay.scoreplay.abstraction.data
import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import java.util.*

interface ScoreRepository {
    /**
     * Gets a score by ID from the specified session
     * @return [Score] if found, null otherwise
     */
    suspend fun getScoreByIdAsync(scoreId: UUID): Score?

    /**
     * Uploads a new score to the session
     */
    suspend fun addScoreAsync(sessionID: UUID, sessionPlayerId: UUID, gameId: Int, score: CreateScoreDto): Score?

    /**
     * Updates an existing score
     */
    suspend fun updateScoreAsync(scoreId: UUID, score: UpdateScoreDto): Score?

    /**
     * Get the scores from a session
     */
    suspend fun getScoresAsync(sessionId: UUID): List<Score>
}
