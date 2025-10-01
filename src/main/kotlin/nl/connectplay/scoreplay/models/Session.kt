package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDateTime
import java.util.UUID

open class Session(
    val id: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val sessionVisibility: Byte) {
}