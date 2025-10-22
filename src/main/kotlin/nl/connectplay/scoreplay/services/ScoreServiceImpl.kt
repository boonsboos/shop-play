package nl.connectplay.scoreplay.services

import kotlinx.datetime.toKotlinLocalDateTime
import nl.connectplay.scoreplay.abstraction.data.ScoreRepository
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.services.ScoreService
import nl.connectplay.scoreplay.exceptions.NotFoundException
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import java.util.*

class ScoreServiceImpl(
    private val scoreRepository: ScoreRepository,
    private val sessionRepository: SessionRepository
) : ScoreService {
    override suspend fun uploadScoreAsync(sessionId: UUID, userId: Int, score: CreateScoreDto): ScoreDto {
        // make sure the session exists
        val session = sessionRepository.getSessionByIdAsync(sessionId, userId)
            ?: throw NotFoundException("Session $sessionId not found")

        // check if the session player exists
        val sessionPlayers = sessionRepository.getSessionPlayers(score.sessionPlayer.userId)
        val currentSessionPlayer = sessionPlayers.firstOrNull { it.guest == score.sessionPlayer.guest }

        val sessionPlayerId = currentSessionPlayer?.sessionPlayerId
            ?: sessionRepository.createSessionPlayerAsync(score.sessionPlayer)?.sessionPlayerId
            ?: throw IllegalStateException("Somehow, we were unable to create a new session player")

        // add the score to the database
        val createdScore = scoreRepository.addScoreAsync(sessionId, sessionPlayerId, session.gameId, score)
            ?: throw IllegalStateException("Failed to add score for ${score.sessionPlayer} to session $sessionId")

        // TODO: FSA-20 Een gebruiker krijgt een notificatie als het leaderboard updatet

        // map score to dto
        return ScoreDto(
            createdScore.scoreId,
            createdScore.score,
            createdScore.turn,
            createdScore.achievedOn.toKotlinLocalDateTime(),
            score.sessionPlayer,
        )
    }

    override suspend fun updateScoreAsync(
        sessionId: UUID,
        userId: Int,
        scoreId: UUID,
        score: UpdateScoreDto
    ): ScoreDto {
        sessionRepository.getSessionByIdAsync(sessionId, userId)
            ?: throw NotFoundException("Session $sessionId not found")

        // check score exists and session ID matches
        val check = scoreRepository.getScoreByIdAsync(scoreId)
        if (sessionId != check?.sessionId) {
            throw IllegalArgumentException("Score $check is not part of session $sessionId or does not exist!")
        }

        // update the score
        val updatedScore = scoreRepository.updateScoreAsync(scoreId, score)
            ?: throw NotFoundException("Score $scoreId not found")

        // map the result to a dto
        val sessionPlayer = sessionRepository.getSessionPlayerAsync(updatedScore.sessionPlayerId)
            ?: throw IllegalStateException("SessionPlayer ${updatedScore.sessionPlayerId} not found after updating game, while it should exist!")

        return ScoreDto(
            updatedScore.scoreId,
            updatedScore.score,
            updatedScore.turn,
            updatedScore.achievedOn.toKotlinLocalDateTime(),
            sessionPlayer.toDto()
        )
    }

    /**
     * @throws NotFoundException when session does not exist
     * @throws IllegalArgumentException when
     */
    override suspend fun getScoreAsync(sessionId: UUID, userId: Int, scoreId: UUID): ScoreDto {
        sessionRepository.getSessionByIdAsync(sessionId, userId)
            ?: throw NotFoundException("Session $sessionId does not exist")

        // check score exists and session ID matches
        val score = scoreRepository.getScoreByIdAsync(scoreId)
            ?: throw NotFoundException("Score $scoreId not found")

        if (sessionId != score.sessionId) {
            throw IllegalArgumentException("Score $score is not part of session $sessionId")
        }

        val sessionPlayer = sessionRepository.getSessionPlayerAsync(score.sessionPlayerId)

        return ScoreDto(
            score.scoreId,
            score.score,
            score.turn,
            score.achievedOn.toKotlinLocalDateTime(),
            sessionPlayer?.toDto()
        )
    }

    override suspend fun getScoresAsync(sessionId: UUID, userId: Int): List<ScoreDto> {
        sessionRepository.getSessionByIdAsync(sessionId, userId)
            ?: throw NotFoundException("Session $sessionId does not exist")

        val scores = scoreRepository.getScoresAsync(sessionId)

        return scores.map { score ->
            val sessionPlayer = sessionRepository.getSessionPlayerAsync(score.sessionPlayerId)

            ScoreDto(
                score.scoreId,
                score.score,
                score.turn,
                score.achievedOn.toKotlinLocalDateTime(),
                sessionPlayer?.toDto()
            )
        }
    }
}