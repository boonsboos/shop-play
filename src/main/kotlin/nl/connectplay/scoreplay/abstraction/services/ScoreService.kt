package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import java.util.*

interface ScoreService {
    /**
     * Uploads a list of scores to a session
     * @param sessionId the ID of the session
     * @param userId the ID of the owner of the session
     * @param scores the list of new scores
     * @return list of populated uploaded scores
     */
    suspend fun bulkUploadScoresAsync(sessionId: UUID, userId: Int, scores: List<CreateScoreDto>): List<ScoreDto>

    /**
     * Updates a specific score
     * @param sessionId the ID of the session
     * @param userId the ID of the owner of the session
     * @param scoreId the ID of the score
     * @return the populated and updated score
     */
    suspend fun updateScoreAsync(sessionId: UUID, userId: Int, scoreId: UUID, score: UpdateScoreDto): ScoreDto

    /**
     * Gets a score in a session
     * @param sessionId the ID of the session
     * @param userId the ID of the owner of the session
     * @param scoreId the ID of the score
     * @return the requested score from the session if found
     */
    suspend fun getScoreAsync(sessionId: UUID, userId: Int, scoreId: UUID): ScoreDto

    /**
     * Gets all scores in a session
     * @param sessionId the ID of the session
     * @param userId the ID of the owner of the session
     * @return a list of all scores in the requested session
     */
    suspend fun getScoresAsync(sessionId: UUID, userId: Int): List<ScoreDto>
}