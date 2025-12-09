package nl.connectplay.scoreplay.models.dto.friend

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.FriendshipStatus
import nl.connectplay.scoreplay.models.dto.user.UserDto

@Serializable
data class UserFriendDto(val user: UserDto, val status: FriendshipStatus)
