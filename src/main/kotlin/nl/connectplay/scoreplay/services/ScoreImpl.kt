package nl.connectplay.scoreplay.services

import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import java.util.UUID

/**
 * Service om data te mappen tussen database entity (Score) en DTOs
 */
class ScoreMapperService {

    /**
     * Map een database entity Score naar ScoreDto (voor API response)
     */
    fun toDto(score: Score): ScoreDto = ScoreDto(
        scoreId = score.scoreId,
        sessionId = score.sessionId,
        sessionPlayerId = score.sessionPlayerId,
        gameId = score.gameId,
        score = score.score,
        turn = score.turn,
        achievedOn = score.achievedOn
    )

    /**
     * Map een lijst van Score naar een lijst van ScoreDto
     */
    fun toDtos(scores: List<Score>): List<ScoreDto> = scores.map { toDto(it) }

    /**
     * Map een CreateScoreDto naar ScoreDto (bijvoorbeeld voor response na aanmaken)
     */
    fun toDto(createDto: CreateScoreDto): ScoreDto = ScoreDto(
        scoreId = createDto.scoreId ,
        sessionId = createDto.sessionId,
        sessionPlayerId = createDto.sessionPlayerId,
        gameId = createDto.gameId,
        score = createDto.score,
        turn = createDto.turn,
        achievedOn = createDto.achievedOn
    )

    /**
     * Map een UpdateScoreDto naar ScoreDto, gebruikmakend van het bestaande scoreId
     */
    fun toDto(updateDto: UpdateScoreDto, existingId: UUID): ScoreDto = ScoreDto(
        scoreId = existingId,
        sessionId = updateDto.sessionId,
        sessionPlayerId = updateDto.sessionPlayerId,
        gameId = updateDto.gameId,
        score = updateDto.score,
        turn = updateDto.turn,
        achievedOn = updateDto.achievedOn
    )
}
