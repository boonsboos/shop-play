package nl.connectplay.scoreplay.models.dto.notifications

import nl.connectplay.scoreplay.models.events.BaseEvent

data class NewNotificationDto(val userId: Int, val notification: BaseEvent)
