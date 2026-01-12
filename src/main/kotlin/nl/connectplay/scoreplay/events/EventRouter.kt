package nl.connectplay.scoreplay.events

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import nl.connectplay.scoreplay.abstraction.data.FollowGameRepository
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.models.dto.notifications.NewNotificationDto
import nl.connectplay.scoreplay.models.events.BaseEvent
import nl.connectplay.scoreplay.models.events.BroadcastEvent
import nl.connectplay.scoreplay.models.events.HighscoreEvent
import nl.connectplay.scoreplay.models.events.SingleTargetEvent
import org.slf4j.LoggerFactory

class EventRouter(
    private val eventQueueManager: EventQueueManagerService,
    private val notificationRepository: NotificationRepository,
    private val followGameRepository: FollowGameRepository
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

        // these operations may take a long time, but we don't have to wait for a result
        // let's run them on separate coroutines
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
            if (!eventQueueManager.enqueueEvent(userId, event)) {
                logger.error("Failed to send event ${event.javaClass.simpleName} to user $userId since they are offline.")
            }
        }
    }

    override suspend fun routeEventAsync(targetUserId: Int, event: SingleTargetEvent): Unit = coroutineScope {
        routeToConnectedUsers(setOf(targetUserId), event)
        notificationRepository.saveNotificationAsync(NewNotificationDto(targetUserId, event))
    }

    private suspend fun getRelevantUserIdsAsync(event: BroadcastEvent): Set<Int> =
        when (event) {
            is HighscoreEvent -> getRelevantUserIdsForHighscoreEvent(event)
        }

    private suspend fun getRelevantUserIdsForHighscoreEvent(event: HighscoreEvent): Set<Int> =
        followGameRepository.getAllFollowerUserIdsAsync(event.game.id).toSet()
}