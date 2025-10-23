package nl.connectplay.scoreplay.models.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDto(val username: String, val email: String, val password: String)