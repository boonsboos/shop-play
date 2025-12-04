package nl.connectplay.scoreplay.models.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class FullUserDto(
    val userId: Int,
    val username: String,
    val email: String,
    val profilePicture: String?
) {
    fun toUserDto(): UserDto = UserDto(userId, username, profilePicture)
}