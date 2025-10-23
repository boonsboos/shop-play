package nl.connectplay.scoreplay.routes.sessions.scores

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.ScoreController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.putScoresRoute() {
    val scoreController by inject<ScoreController>()

    authenticate(UserIdJWTAuthenticatorName) {
        // PATCH /ID — Updating score
        patch("/sessions/{id}/scores/{scoreId}") {
            scoreController.handleUpdateAsync(call)
        }
    }
}