package nl.connectplay.scoreplay.models

import nl.connectplay.scoreplay.models.dto.score.SessionPlayerDto
import java.util.*

data class SessionPlayer(
    val sessionPlayerId: UUID,
    val userId: Int,
    val guest: String? = null
) {
    fun toDto(): SessionPlayerDto =
        SessionPlayerDto(
            userId = userId,
            guest = guest
        )
}
