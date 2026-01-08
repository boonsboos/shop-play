package nl.connectplay.scoreplay.models.dto.session

import kotlinx.serialization.Serializable

@Serializable
data class CreateSessionDto(val gameId: Int, val visibility: Int)