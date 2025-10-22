package nl.connectplay.scoreplay.routes.sessions

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.patch
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.patchSessionsRoutes() {
    val sessionController by inject<SessionController>()
    authenticate(UserIdJWTAuthenticatorName) {
        patch("/sessions/{sessionId}") {
            sessionController.handleUpdateSessionAsync(call)
        }
    }
}