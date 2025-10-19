package nl.connectplay.scoreplay.routes.games

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.deleteFollowGameRoute() {
    val gameController by inject<GameController>()

    delete("/games/{gameId}/follow") {
        gameController.handleUnfollowGame(call)
    }
}