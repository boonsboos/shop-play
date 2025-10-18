package nl.connectplay.scoreplay.routes.games

import io.ktor.server.routing.Route
import io.ktor.server.routing.patch
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesUpdateRoute() {
    val gameController by inject<GameController>()

    patch("/games/{id}") {
        gameController.handleUpdateAsync(call)
    }
}
