package nl.connectplay.scoreplay.models.dto.score

import kotlinx.serialization.Serializable

@Serializable
data class SessionPlayerDto(val userId: Int, val guest: String? = null)