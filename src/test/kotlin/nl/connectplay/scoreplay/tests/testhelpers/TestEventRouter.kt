package nl.connectplay.scoreplay.tests.testhelpers

import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.models.events.BroadcastEvent
import nl.connectplay.scoreplay.models.events.SingleTargetEvent

class TestEventRouter : EventRoutingService {
    override suspend fun routeEventAsync(
        targetUserId: Int,
        event: SingleTargetEvent
    ) {

    }

    override suspend fun routeEventAsync(event: BroadcastEvent) {

    }
}