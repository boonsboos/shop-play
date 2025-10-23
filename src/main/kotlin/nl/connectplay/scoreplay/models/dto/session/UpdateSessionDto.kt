package nl.connectplay.scoreplay.models.dto.session

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.SessionVisibility

@Serializable
data class UpdateSessionDto(
    val endTime: String? = null,
    val visibility: Int? = null
)