package nl.connectplay.scoreplay.events

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.models.events.BaseEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * A service to manage event queues.
 *
 * Used to keep track of which users are connected for receiving events.
 */
class EventQueueManagerServiceImpl : EventQueueManagerService {
    private val queues: ConcurrentHashMap<Int, Channel<BaseEvent>> = ConcurrentHashMap()

    /**
     * Use this empty variable to prevent other threads from accessing a method
     */
    private val lock = Any()

    /**
     * Gets a list of connected user Ids
     * This call gets connected users on a snapshot basis.
     */
    override fun getConnectedUserIds(): Set<Int> =
        queues.keys.toSet()

    /**
     * Writes an event to a queue. Uses synchronisation to guarantee event order.
     * @see Channel.trySend
     */
    override fun enqueueEvent(userId: Int, event: BaseEvent): ChannelResult<Unit>? = synchronized(lock) {
        queues[userId]?.trySend(event)
    }

    /**
     * Provisions and returns a queue for a user. Only use from SSE handler.
     */
    override fun provisionQueue(userId: Int): Channel<BaseEvent> = synchronized(lock) {
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
    override fun removeQueue(userId: Int): Boolean =
        // only one coroutine can call this method at a time
        synchronized(lock) {
            if (queues.containsKey(userId)) {
                queues[userId]?.close()
                queues.remove(userId)
                return true
            }
            return false
        }
}