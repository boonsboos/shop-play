package nl.connectplay.scoreplay.models

import kotlinx.datetime.LocalDateTime
import java.util.*

data class Session(
    val id: UUID,
    val gameId: Int,
    val hostId: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val endOfSessionPicturesId: UUID,
    val sessionVisibility: SessionVisibility
)