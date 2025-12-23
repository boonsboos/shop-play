package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.game.GameDto
import nl.connectplay.scoreplay.models.dto.score.ScoreDto

/**
 * An event to notify users of a new high score set on a game
 * @property game the game on which the score was set
 * @property score the score that was set
 * @property podium at which place the score is - 1st, 2nd or 3rd place.
 */
@Serializable
@SerialName("highscore")
data class HighscoreEvent(val game: GameDto, val score: ScoreDto, val podium: Int) : BroadcastEvent()