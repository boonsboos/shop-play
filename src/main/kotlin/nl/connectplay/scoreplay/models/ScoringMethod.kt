package nl.connectplay.scoreplay.models

/**
 * This enum class that implements [EnumAction].
 *
 * Each value defines its own implementation of [handle],
 * allowing different scoring strategies to be executed
 * depending on the selected enum value.
 *
 * Enum of all scoring methods a game can use.
 *
 * Use [id] for persistence and [name] for UI.
 * Look up by id with [fromId].
 */
enum class ScoringMethod(
    val id: Int,
    val name: String
) : EnumAction {

    HIGHEST_SCORE_WINS(1, "Highest score wins") {
        override fun handle() = println(name)
    },
    LOWEST_SCORE_WINS(2, "Lowest score wins") {
        override fun handle() = println(name)
    },
    FIRST_TO_X_SCORE_WINS(3, "First to X score wins") {
        override fun handle() = println(name)
    },
    FINISHING_ON_POSITION_Y_WINS(4, "Finishing on position Y wins") {
        override fun handle() = println(name)
    },
    LAST_MAN_STANDING_WINS(5, "Last man standing wins") {
        override fun handle() = println(name)
    };
}
