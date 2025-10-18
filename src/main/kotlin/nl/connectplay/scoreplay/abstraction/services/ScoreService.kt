package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto

interface ScoreService {

    suspend fun createScoreAsync(dto: CreateScoreDto): ScoreDto?

    suspend fun updateScoreAsync(scoreId: Int, dto: UpdateScoreDto): ScoreDto?

    suspend fun getScoresAsync(limit: Int? = null, offset: Int? = null): List<ScoreDto>?

    suspend fun getScoreByIdAsync(scoreId: Int): ScoreDto?

    suspend fun getScoresBySessionAsync(sessionId: Int, limit: Int? = null, offset: Int? = null): List<ScoreDto>?
}
