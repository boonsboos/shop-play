package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.Serializable

/**
 * An event indicating a reply to a friend request
 *
 * @param userId the id of the user that received the friend request
 * @param accepts if the user accepted the friend request or not
 */
@Serializable
class FriendRequestReplyEvent(val userId: Int, val accepts: Boolean) : SingleTargetEvent()