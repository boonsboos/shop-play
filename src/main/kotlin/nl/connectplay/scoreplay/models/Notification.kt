package nl.connectplay.scoreplay.models

import java.util.UUID

open class Notification(val id: UUID, val content: String, val read: Boolean) {
}