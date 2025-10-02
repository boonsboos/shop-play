package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDate
import java.util.UUID

open class Score(
    val id: UUID,
    val score: Double,
    var achievedOn: LocalDate,
    var turn: Int?) {
}