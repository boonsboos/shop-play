package nl.connectplay.scoreplay.models

/**
 * This enum class that implements [EnumAction].
 *
 * Similar to [ScoringMethod], but just for the session visibility.
 */
enum class SessionVisibility : EnumAction {
    PRIVATE {
        override fun handle() {
            println("Private")
        }
    },
    FRIENDS_ONLY {
        override fun handle() {
            println("Friends only")
        }
    },
    ANONYMISED {
        override fun handle() {
            println("Anonymised except host")
        }
    },
    PUBLIC {
        override fun handle() {
            println("Public")
        }
    };

    fun toInt() = when (this) {
        PRIVATE -> 0
        FRIENDS_ONLY -> 1
        ANONYMISED -> 2
        PUBLIC -> 3
    }
}