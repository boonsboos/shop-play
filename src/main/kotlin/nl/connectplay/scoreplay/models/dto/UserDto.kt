package nl.connectplay.scoreplay.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val username: String, val profilePicture: String?)