package nl.connectplay.scoreplay.utilities

import io.ktor.server.application.*
import java.util.*

/**
 * Utility method for getting a UUID from a path parameter
 */
fun ApplicationCall.getUUIDOrNull(param: String): UUID? =
    try {
        UUID.fromString(this.parameters[param])
    } catch (_: IllegalArgumentException) {
        null
    }