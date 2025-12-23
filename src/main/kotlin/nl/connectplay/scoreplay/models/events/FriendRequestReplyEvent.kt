package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.user.UserDto

/**
 * An event indicating a reply to a friend request
 *
 * @param respondingUser the user that received the friend request
 * @param accepts if the user accepted the friend request or not
 */
@Serializable
@SerialName("friendRequestReply")
class FriendRequestReplyEvent(val respondingUser: UserDto, val accepts: Boolean) : SingleTargetEvent()
