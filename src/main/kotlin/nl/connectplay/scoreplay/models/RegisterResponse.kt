package nl.connectplay.scoreplay.models

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.user.CreateUserDto

@Serializable
data class RegisterResponse(
    val data: CreateUserDto? = null,
    val message: String? = null
)
