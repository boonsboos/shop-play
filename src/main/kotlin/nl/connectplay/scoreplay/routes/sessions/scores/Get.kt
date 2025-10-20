package nl.connectplay.scoreplay.routes.sessions.scores

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.ScoreController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getScoresRoute() {
    val scoreController by inject<ScoreController>()

    // GET /scores — List of scores
    get("sessions/{id}/scores") {
        scoreController.handleListAsync(call)
    }

    // GET /scores/{id} — Give score by ID
    get("/sessions/{id}/scores/{scoreId}") {
        scoreController.handleOneAsync(call)
    }
}