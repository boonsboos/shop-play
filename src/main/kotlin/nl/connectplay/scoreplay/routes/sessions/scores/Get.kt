package nl.connectplay.scoreplay.routes.sessions.scores

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.ScoreController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getScoresRoute() {
    val scoreController by inject<ScoreController>()
    authenticate(UserIdJWTAuthenticatorName) {
        // GET /scores — List of scores
        get("/sessions/{sessionId}/scores") {
            scoreController.handleListAsync(call)
        }

        // GET /scores/{id} — Give score by ID
        get("/sessions/{sessionId}/scores/{scoreId}") {
            scoreController.handleOneAsync(call)
        }
    }
}