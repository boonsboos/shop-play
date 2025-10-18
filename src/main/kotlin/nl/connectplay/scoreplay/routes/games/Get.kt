package nl.connectplay.scoreplay.routes.games

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesRoute() {
    val gameController by inject<GameController>()

    get("/games") {
        gameController.handleListAsync(call)
    }
}
