package nl.connectplay.scoreplay.routes.notifications

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.seconds

@ApiRoute
fun Route.notificationEvents() {
    val notificationController by inject<NotificationController>()

    authenticate(UserIdJWTAuthenticatorName) {
        sse (
            "/notifications/live",
            // SSE requires manually configuring serialization
            serialize = { typeInfo, it ->
                val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
                Json.encodeToString(serializer, it)
            }
        ) {
            heartbeat {
                period = 15.seconds
            }

            try {
                notificationController.handleSseSession(this)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Tried to start SSE session but user was unauthorized", e)
                return@sse call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}