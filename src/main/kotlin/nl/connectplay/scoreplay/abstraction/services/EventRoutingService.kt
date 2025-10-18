package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.events.BaseEvent

interface EventRoutingService {
    suspend fun routeEventAsync(event: BaseEvent)
}