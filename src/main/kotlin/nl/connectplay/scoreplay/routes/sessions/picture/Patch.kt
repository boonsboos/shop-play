package nl.connectplay.scoreplay.routes.sessions.picture

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.sessionUploadPictureRoute() {
    val sessionController by inject<SessionController>()

    authenticate(UserIdJWTAuthenticatorName) {
        patch("/sessions/{id}/picture") {
            sessionController.handleUploadPictureAsync(call)
        }
    }
}
