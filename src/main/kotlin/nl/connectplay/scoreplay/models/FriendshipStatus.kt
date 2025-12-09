package nl.connectplay.scoreplay.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipStatus {
    /**
     * Rejected friendship.
     * Use in notifications only to notify users their friend request has been rejected
     */
    @SerialName("rejected")
    REJECTED,

    /**
     * Pending friendship.
     * The user has to wait for the other user to respond
     */
    @SerialName("pending")
    PENDING,

    @SerialName("accepted")
    ACCEPTED,

    /**
     * Active friendship.
     * The users can see things meant only for friends.
     */
    @SerialName("friends")
    FRIENDS
}