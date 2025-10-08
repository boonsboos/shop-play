package nl.connectplay.scoreplay.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserDto(val username: String, val email: String, val password: String)