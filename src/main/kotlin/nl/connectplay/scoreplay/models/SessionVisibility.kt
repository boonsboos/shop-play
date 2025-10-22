package nl.connectplay.scoreplay.models

enum class SessionVisibility {
    PRIVATE,
    FRIENDS_ONLY,
    ANONYMISED,
    PUBLIC;

    fun toInt() = when (this) {
        PRIVATE -> 0
        FRIENDS_ONLY -> 1
        ANONYMISED -> 2
        PUBLIC -> 3
    }

    fun isPublic() = this == PUBLIC || this == ANONYMISED

    companion object {
        fun fromInt(visibility: Int) = when (visibility) {
            0 -> PRIVATE
            1 -> FRIENDS_ONLY
            2 -> ANONYMISED
            3 -> PUBLIC
            else -> ANONYMISED
        }
    }
}