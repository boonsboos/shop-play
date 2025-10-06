package nl.connectplay.scoreplay.routes.sessions

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.createSession() {
    val sessionController = SessionController()

    post("/sessions") {
        sessionController.handleSessionCreation(call)
    }
}