package nl.connectplay.scoreplay.routes.scores

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.ScoreController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import nl.connectplay.scoreplay.services.ScoreMapperService
import nl.connectplay.scoreplay.abstraction.services.ScoreService

@ApiRoute
fun Route.getScoresRoute() {
    val scoreController by inject<ScoreController>()

    // GET /scores — List of scores
    get("/scores") {
        scoreController.handleListAsync(call)
    }

    // GET /scores/{id} — Give score by ID
    get("/scores/{id}") {
        scoreController.handleOneAsync(call)
    }
}