package nl.connectplay.scoreplay.models

/**
 * This enum class that implements [EnumAction].
 *
 * Each value defines its own implementation of [handle],
 * allowing different scoring strategies to be executed
 * depending on the selected enum value.
 */
enum class ScoringMethod : EnumAction {
    HIGHEST_WINS {
        override fun handle() {
            println("Highest wins")
        }
    },
    LOWEST_WINS {
        override fun handle() {
            println("Lowest wins")
        }
    },
    LEAST_FAILS {
        override fun handle() {
            println("Least fails")
        }
    },
}