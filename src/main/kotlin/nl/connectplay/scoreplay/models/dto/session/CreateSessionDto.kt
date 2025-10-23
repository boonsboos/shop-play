package nl.connectplay.scoreplay.models.dto.session

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.SessionVisibility

@Serializable
data class CreateSessionDto(val gameId: Int, val userId: Int, val visibility: Int)