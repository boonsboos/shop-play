package nl.connectplay.scoreplay.routes.users.sessions

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getUserSessionsRoute() {
    val sessionController by inject<SessionController>()
    authenticate(UserIdJWTAuthenticatorName) {
        // Get all sessions that belong to the requested user
        get("/users/{targetId}/sessions") {
            sessionController.handleListAsync(call)
        }

        // Get a specific session by ID for the requested user
        get("/users/{targetId}/sessions/{sessionId}") {
            sessionController.handleOneAsync(call)
        }
    }
}