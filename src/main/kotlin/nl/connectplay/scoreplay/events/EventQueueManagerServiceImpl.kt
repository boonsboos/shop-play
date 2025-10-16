package nl.connectplay.scoreplay.events

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.models.events.BaseEvent

/**
 * A service to manage event queues.
 *
 * Used to keep track of which users are connected for receiving events.
 */
class EventQueueManagerServiceImpl : EventQueueManagerService {
    // ideally we'd use some kind of locking on the channel
    // as well as the map itself to guarantee safe modifications
    // and allow only a single connection to have access to the channel
    private val queues: MutableMap<Int, Channel<BaseEvent>> = mutableMapOf()

    /**
     * Gets a list of connected user Ids
     */
    override fun getConnectedUserIds(): Set<Int> = queues.keys.toSet()

    /**
     * Writes an event to a queue
     * @see Channel.trySend
     */
    override fun enqueueEvent(userId: Int, event: BaseEvent): ChannelResult<Unit>? =
        queues[userId]?.trySend(event)

    /**
     * Gets a queue for the user.
     */
    override fun getQueue(userId: Int): Channel<BaseEvent> {
        // if there is no queue yet, create one
        if (!queues.containsKey(userId)) {
            queues[userId] = Channel(Channel.BUFFERED)
        }

        // we must delegate responsibility over the channel to the programmer
        return queues[userId] ?: throw IllegalStateException("Attempted to get queue for user $userId, but the queue was removed!")
    }

    /**
     * Removes a queue from the registry
     */
    override fun removeQueue(userId: Int): Boolean {
        if (queues.containsKey(userId)) {
            queues[userId]?.close()
            queues.remove(userId)
            return true
        }
        return false
    }
}