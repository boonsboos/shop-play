package nl.connectplay.scoreplay.models

/**
 * Enum of all scoring methods a game can use.
 *
 */
enum class ScoringMethod {
    HIGHEST_SCORE_WINS,
    LOWEST_SCORE_WINS,
    FIRST_TO_X_SCORE_WINS,
    LAST_TO_X_SCORE_WINS,
    FINISHING_ON_POSITION_Y_WINS,
    LAST_MAN_STANDING_WINS;

    fun toInt() = when (this) {
        HIGHEST_SCORE_WINS -> 0
        LOWEST_SCORE_WINS -> 1
        FIRST_TO_X_SCORE_WINS -> 2
        LAST_TO_X_SCORE_WINS -> 3
        FINISHING_ON_POSITION_Y_WINS -> 4
        LAST_MAN_STANDING_WINS -> 5
    }

    companion object {
        fun fromInt(score: Int) = when (score) {
            0 -> HIGHEST_SCORE_WINS
            1 -> LOWEST_SCORE_WINS
            2 -> FIRST_TO_X_SCORE_WINS
            3 -> LAST_TO_X_SCORE_WINS
            4 -> FINISHING_ON_POSITION_Y_WINS
            5 -> LAST_MAN_STANDING_WINS
        }
    }
}
