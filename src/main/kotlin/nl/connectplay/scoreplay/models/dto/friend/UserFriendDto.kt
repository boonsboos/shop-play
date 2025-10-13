package nl.connectplay.scoreplay.models.dto.friend

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.FriendshipStatus

@Serializable
data class UserFriendDto(val username: String, val profilePicture: String? = null, val status: FriendshipStatus)
