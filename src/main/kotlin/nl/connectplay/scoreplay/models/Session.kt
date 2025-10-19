package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDateTime
import java.util.*

data class Session(
    val id: UUID,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val sessionVisibility: SessionVisibility
)