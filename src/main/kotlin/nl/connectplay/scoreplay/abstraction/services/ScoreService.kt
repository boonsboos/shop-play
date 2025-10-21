package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import java.util.*

interface ScoreService {
    suspend fun uploadScoreAsync(sessionId: UUID, userId: Int, score: CreateScoreDto): ScoreDto
    suspend fun updateScoreAsync(sessionId: UUID, userId: Int, scoreId: UUID, score: UpdateScoreDto): ScoreDto
    suspend fun getScoreAsync(sessionId: UUID, userId: Int, scoreId: UUID): ScoreDto
    suspend fun getScoresAsync(sessionId: UUID, userId: Int): List<ScoreDto>
}