package nl.connectplay.scoreplay.abstraction.data
import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.dto.ScoreDto
import java.util.UUID

interface ScoreRepository {
    // because of 'suspend', the function will run asynchronously

    // get score by ID
    suspend fun getScoreByIdAsync(score: UUID): Score?

    // Add new score
    suspend fun addScoreAsync(score: ScoreDto) : Boolean

    // Update existing score
    suspend fun updateScoreAsync(score: ScoreDto) : Boolean

    // get multiple scores
    suspend fun getScoresAsync(limit: Int? = 25, offset: Int? = 0, userId: UUID? = null): List<ScoreDto>?
}
