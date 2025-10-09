package nl.connectplay.scoreplay.models.dto.friend

import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.FriendshipStatus

@Serializable
data class FriendRequestResponseDto(val friendId: Int, val status: FriendshipStatus = FriendshipStatus.PENDING)
