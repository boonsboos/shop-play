package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.Serializable

/**
 * Base event for implementing an event that is meant to broadcast to multiple users.
 */
@Serializable
sealed class BroadcastEvent : BaseEvent()