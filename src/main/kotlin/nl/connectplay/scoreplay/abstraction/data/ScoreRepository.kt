package nl.connectplay.scoreplay.abstraction.data
import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import java.util.UUID

interface ScoreRepository {
    // because of 'suspend', the function will run asynchronously

    // get score by ID
    suspend fun getScoreByIdAsync(score: UUID): Score?

    // Add new score
    suspend fun addScoreAsync(score: CreateScoreDto) : Boolean

    // Update existing score
    suspend fun updateScoreAsync(score: UpdateScoreDto) : Boolean

    // get multiple scores
    suspend fun getScoresAsync(limit: Int? = 25, offset: Int? = 0, sessionPlayerId: UUID? = null): List<ScoreDto>?
}
