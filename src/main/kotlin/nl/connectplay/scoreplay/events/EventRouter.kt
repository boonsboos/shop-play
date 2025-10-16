package nl.connectplay.scoreplay.events

import kotlinx.coroutines.channels.onFailure
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.events.BaseEvent
import org.slf4j.LoggerFactory

class EventRouter(
    private val eventQueueManager: EventQueueManagerService,
    private val notificationRepository: NotificationRepository,
): EventRoutingService {
    private val logger = LoggerFactory.getLogger(EventRouter::class.java)

    override suspend fun routeEvent(event: BaseEvent) {
        // 1. get users that need to be notified of the event
        val relevantUsers = getRelevantUserIdsAsync(event)

        // we don't have to route events when there are no relevant users
        if (relevantUsers.isEmpty()) return

        val connectedUsers = eventQueueManager.getConnectedUserIds()

        // get the user IDs that correspond with the connected users
        val connectedRelevantUsers = (relevantUsers intersect connectedUsers)

        // 2a. if they are connected, enqueue the event
        for (userId in connectedRelevantUsers) {
            eventQueueManager.enqueueEvent(userId, event)?.onFailure {
                // TODO: more expansive logging might be useful
                logger.error("Failed to send event ${event.javaClass.simpleName} to connected user $userId. Queue has likely been closed")
            }
        }

        // 2b. save the notification for later retrieval and for users that are not connected
        for (userId in relevantUsers) {
            notificationRepository.saveNotificationAsync(NewNotificationDto(userId, event))
        }
    }

    suspend fun getRelevantUserIdsAsync(event: BaseEvent): Set<Int> =
        when (event) {
            is BaseEvent -> setOf()
        }

    //
    suspend fun getRelevantUserIdForFriendRequest() {

    }
}