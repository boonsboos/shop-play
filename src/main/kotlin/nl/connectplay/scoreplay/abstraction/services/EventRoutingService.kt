package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.events.BroadcastEvent
import nl.connectplay.scoreplay.models.events.SingleTargetEvent

interface EventRoutingService {
    suspend fun routeEventAsync(targetUserId: Int, event: SingleTargetEvent)
    suspend fun routeEventAsync(event: BroadcastEvent)
}