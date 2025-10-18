package nl.connectplay.scoreplay.abstraction.services

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import nl.connectplay.scoreplay.models.events.BaseEvent

interface EventQueueManagerService {
    /**
     * Get the user IDs of users currently connected for events
     */
    fun getConnectedUserIds(): Set<Int>

    /**
     * Writes an event to the connected user's event queue
     */
    fun enqueueEvent(userId: Int, event: BaseEvent): ChannelResult<Unit>?

    /**
     * Gets the event queue for the user.
     */
    fun provisionQueue(userId: Int): Channel<BaseEvent>

    /**
     * Removes and closes a queue. Use when a user disconnects.
     */
    fun removeQueue(userId: Int): Boolean
}