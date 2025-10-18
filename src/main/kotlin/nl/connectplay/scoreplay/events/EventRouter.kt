package nl.connectplay.scoreplay.events

import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.events.BaseEvent
import nl.connectplay.scoreplay.models.events.BroadcastEvent
import nl.connectplay.scoreplay.models.events.SingleTargetEvent
import org.slf4j.LoggerFactory

class EventRouter(
    private val eventQueueManager: EventQueueManagerService,
    private val notificationRepository: NotificationRepository,
): EventRoutingService {
    private val logger = LoggerFactory.getLogger(EventRouter::class.java)

    override suspend fun routeEventAsync(event: BroadcastEvent): Unit = coroutineScope {
        // 1. get users that need to be notified of the event
        val relevantUsers = getRelevantUserIdsAsync(event)

        // we don't have to route the event when there are no relevant users
        if (relevantUsers.isEmpty()) return@coroutineScope

        val connectedUsers = eventQueueManager.getConnectedUserIds()

        // get the user IDs that correspond with the connected users
        val connectedRelevantUsers = (relevantUsers intersect connectedUsers)

        // these operations may take a long time, but we don't have to wait
        launch {
            // 2a. if they are connected, enqueue the event
            routeToConnectedUsers(connectedRelevantUsers, event)
        }
        launch {
            // 2b. save the notification for later retrieval and for users that are not connected
            for (userId in relevantUsers) {
                notificationRepository.saveNotificationAsync(NewNotificationDto(userId, event))
            }
        }
    }

    private fun routeToConnectedUsers(connectedRelevantUsers: Set<Int>, event: BaseEvent) {
        for (userId in connectedRelevantUsers) {
            eventQueueManager.enqueueEvent(userId, event)?.onFailure {
                // TODO: more expansive logging might be useful
                logger.error("Failed to send event ${event.javaClass.simpleName} to connected user $userId. Queue has likely been closed")
            }
        }
    }

    override suspend fun routeEventAsync(targetUserId: Int, event: SingleTargetEvent): Unit = coroutineScope {
        routeToConnectedUsers(setOf(targetUserId), event)
        notificationRepository.saveNotificationAsync(NewNotificationDto(targetUserId, event))
    }

    private suspend fun getRelevantUserIdsAsync(event: BroadcastEvent): Set<Int> =
        // not all events should be routed
        when (event) {
            else -> emptySet()
        }

    private suspend fun getRelevantUserIdForFriendRequest() {
        TODO("implement getting relevant user ids for a friend request event")
    }
}