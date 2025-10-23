package nl.connectplay.scoreplay.models.dto.score

import kotlinx.serialization.Serializable

/**
 * Used to indicate who set a score. If userId is 0, the score has been anonymised.
 *
 * @property userId the user that created the session player
 * @property guest the name of a guest player (if present)
 *
 */
@Serializable
data class SessionPlayerDto(val userId: Int, val guest: String? = null)