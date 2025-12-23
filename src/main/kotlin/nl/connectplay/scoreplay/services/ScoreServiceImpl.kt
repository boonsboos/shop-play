package nl.connectplay.scoreplay.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinLocalDateTime
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.abstraction.data.LeaderboardRepository
import nl.connectplay.scoreplay.abstraction.data.ScoreRepository
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.abstraction.services.ScoreService
import nl.connectplay.scoreplay.exceptions.NotFoundException
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.exceptions.UnfinishedSessionException
import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.SessionPlayer
import nl.connectplay.scoreplay.models.SessionVisibility
import nl.connectplay.scoreplay.models.dto.leaderboard.LeaderboardEntryDto
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.SessionPlayerDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import nl.connectplay.scoreplay.models.events.HighscoreEvent
import java.util.*

class ScoreServiceImpl(
    private val scoreRepository: ScoreRepository,
    private val sessionRepository: SessionRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val gameRepository: GameRepository,
    private val eventRouter: EventRoutingService
) : ScoreService {
    override suspend fun bulkUploadScoresAsync(
        sessionId: UUID,
        userId: Int,
        scores: List<CreateScoreDto>
    ): List<ScoreDto> {
        // make sure the session exists
        val session = sessionRepository.getSessionByIdAsync(sessionId)
            ?: throw NotFoundException("Session $sessionId not found")

        if (session.hostId != userId) {
            throw UnauthorizedException("You are not the host")
        }

        if (session.endTime == null) {
            throw UnfinishedSessionException(userId, sessionId)
        }

        // NOTE: it might be good to add a lock on this method to prevent race conditions.
        // Sadly, on the JVM it's up to the JVM vendor whether a lock maintains a queue of those next in line to enter or not.

        // take a snapshot of the top 3 before uploading
        // this list is already sorted by score and date
        val leaderboardScores = leaderboardRepository.getTopScoresForGame(session.game.id)
            .take(3)

        val newScores: Map<SessionPlayer, Score> = scores.associate { score ->
            uploadScoreAsync(session, score)
        }

        // we can only broadcast the score if the session is publicly viewable
        // since deciding whether a score is a high score or not may take a long time depending on how many scores were uploaded,
        // it should run in a separate coroutine
        if (session.visibility.isPublic()) {
            withContext(Dispatchers.Default) {
                launch {
                    tryBroadcastHighscore(leaderboardScores, newScores, session)
                }
            }
        }

        // map scores to dto
        return newScores.map { (player, score) ->
            ScoreDto(
                score.scoreId,
                score.score,
                score.turn,
                score.achievedOn.toKotlinLocalDateTime(),
                player.toDto(), // we don't need to anonymize here since the user uploading is the owner of the session
            )
        }
    }

    private suspend fun tryBroadcastHighscore(
        leaderboardScores: List<LeaderboardEntryDto>,
        playerScores: Map<SessionPlayer, Score>,
        session: SessionDto
    ) {
        val game = this.gameRepository.getGameByIdAsync(session.game.id)
            ?: throw IllegalStateException("Game ${session.game.id} was deleted while the session was submitting scores")

        val top3: MutableList<Pair<SessionPlayer, Score>> = mutableListOf()

        // determine new top 3 with a sliding window technique
        for ((player, score) in playerScores) {
            for (leaderboardScore in leaderboardScores) {
                // not this high a score if less than or equal
                if (leaderboardScore.score >= score.score) continue

                // store the score in the new top 3
                top3.addFirst(player to score)
                if (top3.size > 3) { // if we have more than 3 scores now, remove the last one
                    top3.removeLast()
                }
            }
        }

        // broadcast the event, if any
        for ((index, pair) in top3.withIndex()) {
            val player = pair.first
            val score = pair.second

            // anonymise the player that set the score if applicable
            val processedPlayer = when (session.visibility) {
                SessionVisibility.PUBLIC -> player.toDto()
                SessionVisibility.ANONYMISED -> SessionPlayerDto(player.userId, "Anonymous")
                else -> throw IllegalStateException(
                    "Broadcasting a highscore event from a session with non-public visibility is not allowed. " +
                            "(Session: ${session.sessionId})"
                )
            }

            // we route the event, because this is a high score
            eventRouter.routeEventAsync(
                HighscoreEvent(
                    game = game.withPictures(listOf()),
                    score = ScoreDto(
                        score.scoreId,
                        score.score,
                        score.turn,
                        score.achievedOn.toKotlinLocalDateTime(),
                        processedPlayer
                    ),
                    podium = index + 1 // index starts from 0
                )
            )
        }
    }

    private suspend fun uploadScoreAsync(session: SessionDto, score: CreateScoreDto): Pair<SessionPlayer, Score> {
        // get the right session player or create a new one if it does not exist yet
        val sessionPlayers = sessionRepository.getSessionPlayers(score.sessionPlayer.userId)
        val currentSessionPlayer = sessionPlayers.firstOrNull { it.guest == score.sessionPlayer.guest }
            ?: sessionRepository.createSessionPlayerAsync(score.sessionPlayer)
            ?: throw NotFoundException("Somehow, we were unable to find a fitting session player")

        // add the score to the database
        val score = scoreRepository.addScoreAsync(
            session.sessionId,
            currentSessionPlayer.sessionPlayerId,
            session.game.id,
            score
        )
            ?: throw IllegalStateException("Failed to add score for ${score.sessionPlayer} to session ${session.sessionId}")

        return (currentSessionPlayer to score)
    }

    override suspend fun updateScoreAsync(
        sessionId: UUID,
        userId: Int,
        scoreId: UUID,
        score: UpdateScoreDto
    ): ScoreDto {
        val session = sessionRepository.getSessionByIdAsync(sessionId)
            ?: throw NotFoundException("Session $sessionId not found")

        if (session.hostId != userId) {
            throw UnauthorizedException("You are not the host")
        }

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
        val session = sessionRepository.getSessionByIdAsync(sessionId)
            ?: throw NotFoundException("Session $sessionId does not exist")

        // check score exists and session ID matches
        val score = scoreRepository.getScoreByIdAsync(scoreId)
            ?: throw NotFoundException("Score $scoreId not found")

        if (sessionId != score.sessionId) {
            throw IllegalArgumentException("Score $score is not part of session $sessionId")
        }

        val sessionPlayer = sessionRepository.getSessionPlayerAsync(score.sessionPlayerId)
            ?: throw NotFoundException("SessionPlayer ${score.sessionPlayerId} not found")

        return ScoreDto(
            score.scoreId,
            score.score,
            score.turn,
            score.achievedOn.toKotlinLocalDateTime(),
            sessionPlayer.toDto()
        )
    }

    override suspend fun getScoresAsync(sessionId: UUID, userId: Int): List<ScoreDto> {
        sessionRepository.getSessionByIdAsync(sessionId)
            ?: throw NotFoundException("Session $sessionId does not exist")

        val scores = scoreRepository.getScoresAsync(sessionId)

        return scores.map { score ->
            val sessionPlayer = sessionRepository.getSessionPlayerAsync(score.sessionPlayerId)
                ?: throw NotFoundException("SessionPlayer ${score.sessionPlayerId} not found")

            ScoreDto(
                score.scoreId,
                score.score,
                score.turn,
                score.achievedOn.toKotlinLocalDateTime(),
                sessionPlayer.toDto()
            )
        }
    }
}