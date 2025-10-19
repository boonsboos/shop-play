package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import java.util.*

interface ScoreService {
    suspend fun uploadScoreAsync(sessionId: UUID, score: CreateScoreDto): ScoreDto
    suspend fun updateScoreAsync(sessionId: UUID, scoreId: UUID, score: UpdateScoreDto): ScoreDto
    suspend fun getScoreAsync(sessionId: UUID, scoreId: UUID): ScoreDto
    suspend fun getScoresAsync(sessionId: UUID): List<ScoreDto>
}