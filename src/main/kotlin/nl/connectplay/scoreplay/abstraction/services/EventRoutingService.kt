package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.events.BroadcastEvent
import nl.connectplay.scoreplay.models.events.SingleTargetEvent

interface EventRoutingService {
    /**
     * Routes a targeted event to a single target user
     */
    suspend fun routeEventAsync(targetUserId: Int, event: SingleTargetEvent)

    /**
     * Route a broadcast event to multiple users
     */
    suspend fun routeEventAsync(event: BroadcastEvent)
}