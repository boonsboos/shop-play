package nl.connectplay.scoreplay.routes.scores

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.ScoreController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.postScoresRoute() {
    val scoreController by inject<ScoreController>()

    // POST /scores — Creating score
    post("/sessions/{id}/scores") {
        scoreController.handleCreateAsync(call)
    }
}