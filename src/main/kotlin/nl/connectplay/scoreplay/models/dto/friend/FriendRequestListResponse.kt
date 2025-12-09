package nl.connectplay.scoreplay.models.dto.friend

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequestListResponse(val pending: List<UserFriendDto>, val outstanding: List<UserFriendDto>)