package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An event indicating a friend request.
 *
 * @param friendId the user initiating the friend request
 */
@Serializable
@SerialName("friendRequest")
class FriendRequestEvent(val targetUserId: Int) : SingleTargetEvent()
