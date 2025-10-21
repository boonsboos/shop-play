package nl.connectplay.scoreplay.models

/**
 * Represents all possible scoring methods a game can use.
 *
 * Each scoring method is defined as a singleton `data object` with a unique [id] and [name].
 *
 * The [companion object] provides a helper function [fromId] to look up
 * a specific [ScoringMethod] instance by its id.
 */
sealed class ScoringMethod(
    val id: Int,
    val name: String
) {
    // Singleton for a single fixed instance
    data object HighestScoreWins : ScoringMethod(1, "Highest score wins")
    data object LowestScoreWins : ScoringMethod(2, "Lowest score wins")
    data object FirstToXScoreWins : ScoringMethod(3, "First to X score wins")
    data object FinishingOnPositionYWins : ScoringMethod(4, "Finishing on position Y wins")
    data object LastManStandingWins : ScoringMethod(5, "Last man standing wins")

    // Static holder for factory functions
    companion object {
        fun fromId(id: Int): ScoringMethod? = when (id) {
            1 -> HighestScoreWins
            2 -> LowestScoreWins
            3 -> FirstToXScoreWins
            4 -> FinishingOnPositionYWins
            5 -> LastManStandingWins
            else -> null
        }
    }
}