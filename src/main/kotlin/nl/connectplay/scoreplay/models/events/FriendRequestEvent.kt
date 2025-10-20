package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.Serializable

/**
 * An event indicating a friend request.
 *
 * @param friendId the user initiating the friend request
 */
@Serializable
class FriendRequestEvent(val friendId: Int) : SingleTargetEvent()