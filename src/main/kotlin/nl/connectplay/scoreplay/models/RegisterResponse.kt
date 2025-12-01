package nl.connectplay.scoreplay.models

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.user.UserDto

@Serializable
open class RegisterResponse(
    val data: UserDto? = null,
    val message: String? = null
)
