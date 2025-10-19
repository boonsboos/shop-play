package nl.connectplay.scoreplay.routes.games

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesFollowRoute() {
    val gameController by inject<GameController>()

    post("/games/{gameId}/follow") {
        gameController.handleFollowGame(call)
    }

    post("/games/{gameId}/follow") {
        gameController.handleUnfollowGame(call)
    }
}

