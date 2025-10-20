package nl.connectplay.scoreplay.models

import nl.connectplay.scoreplay.models.events.BaseEvent
import java.util.UUID

open class Notification(
    val notificationId: UUID,
    val userId: Int,
    val content: String,
    val read: Boolean){
}