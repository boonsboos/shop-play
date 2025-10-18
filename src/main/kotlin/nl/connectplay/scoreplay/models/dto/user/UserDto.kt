package nl.connectplay.scoreplay.models.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val username: String, val email: String, val profilePicture: String?)