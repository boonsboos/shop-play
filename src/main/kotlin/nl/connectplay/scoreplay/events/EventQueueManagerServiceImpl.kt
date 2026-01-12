package nl.connectplay.scoreplay.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.models.events.BaseEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * A service to manage event queues.
 *
 * Used to keep track of which users are connected for receiving events.
 */
class EventQueueManagerServiceImpl : EventQueueManagerService {

    private val logger: Logger = LoggerFactory.getLogger(EventQueueManagerServiceImpl::class.java)

    private val queues: ConcurrentHashMap<Int, MutableSharedFlow<BaseEvent>> = ConcurrentHashMap()

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
     * Writes an event to a queue. Uses synchronisation to ensure queue deletions are handled properly.
     * @see MutableSharedFlow.tryEmit
     * @return false when the event was not sent because the user was disconnected, true when the event was sent out successfully
     */
    override fun enqueueEvent(userId: Int, event: BaseEvent): Boolean = synchronized(lock) {
        val queue = queues[userId] ?: return false // no queue means no event was written
        if (!queue.tryEmit(event)) {
            // queue is full since we have no space left in the buffer
            // that means the user is completely disconnected, and we should remove the queue
            logger.info("Cleaning up event queue for user $userId as we've tried twice to send to a closed connection.")
            queues.remove(userId)
            false
        } else {
            true
        }
    }

    /**
     * Provisions and returns a queue for a user. Only use from SSE handler.
     */
    override fun provisionQueue(userId: Int): SharedFlow<BaseEvent> {
        // if there is no queue yet, create one
        return queues.computeIfAbsent(userId) {
            // replay = 0 means any new collectors will not receive past events
            // extraBufferCapacity means we only allow 1 event on the queue at a time.
            // this is okay, because they will immediately be collected unless the user is disconnected
            // at which point we remove the queue while emitting the event
            MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
        }.asSharedFlow()
    }
}