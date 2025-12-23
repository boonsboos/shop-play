package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.user.UserDto

/**
 * An event indicating a friend request.
 *
 * @param from the user initiating the friend request
 */
@Serializable
@SerialName("friendRequest")
class FriendRequestEvent(val from: UserDto) : SingleTargetEvent()
