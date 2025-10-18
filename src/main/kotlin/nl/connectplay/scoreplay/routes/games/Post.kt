package nl.connectplay.scoreplay.routes.games

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesCreateRoute() {
    val gameController by inject<GameController>()

    post("/games") {
        gameController.handleCreateAsync(call)
    }
}
