package nl.connectplay.scoreplay.models.events

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A base class for defining events within the system
 */
@OptIn(ExperimentalTime::class)
@Serializable
sealed class BaseEvent(val label: String = "base", val created: Instant = Clock.System.now()) {
}