package nl.connectplay.scoreplay.abstraction.services

import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.flow.SharedFlow
import nl.connectplay.scoreplay.models.events.BaseEvent

interface EventQueueManagerService {
    /**
     * Get the user IDs of users currently connected for events
     * @return [Set] with the IDs of users currently with an open queue
     */
    fun getConnectedUserIds(): Set<Int>

    /**
     * Writes an event to the connected user's event queue
     * @param userId the id of the connected user
     * @param event the event to send to the connect user
     * @return [ChannelResult]
     */
    fun enqueueEvent(userId: Int, event: BaseEvent): Boolean

    /**
     * Provision the event queue for the user.
     * @param userId the id of the user to provision a queue for
     */
    fun provisionQueue(userId: Int): SharedFlow<BaseEvent>
}