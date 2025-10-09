package nl.connectplay.scoreplay.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipStatus {
    @SerialName("pending")
    PENDING,
    @SerialName("friends")
    FRIENDS
}