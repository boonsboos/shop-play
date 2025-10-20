package nl.connectplay.scoreplay.routes.sessions.scores

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.ScoreController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.putScoresRoute() {
    val scoreController by inject<ScoreController>()

    patch("/sessions/{id}/scores/{scoreId}") {
        scoreController.handleUpdateAsync(call)
    }
}