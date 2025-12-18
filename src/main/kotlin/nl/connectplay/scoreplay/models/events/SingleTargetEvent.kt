package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Indicates an event is meant to target only a single user, to optimize processing
 *
 * @param target optional target user ID
 */
@Serializable
@SerialName("singleTarget")
sealed class SingleTargetEvent(var target: Int? = null) : BaseEvent()