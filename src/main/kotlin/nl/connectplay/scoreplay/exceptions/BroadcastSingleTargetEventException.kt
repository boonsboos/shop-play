package nl.connectplay.scoreplay.exceptions

import nl.connectplay.scoreplay.models.events.BaseEvent

class BroadcastSingleTargetEventException(event: BaseEvent)
    : RuntimeException("Event ${event::class.simpleName} only supports single targeting and should not be routed using the broadcast event")