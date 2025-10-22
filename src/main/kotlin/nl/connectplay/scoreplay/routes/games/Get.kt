package nl.connectplay.scoreplay.routes.games

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesRoute() {
    val gameController by inject<GameController>()

    get("/games") {
        gameController.handleListAsync(call)
    }

    authenticate(UserIdJWTAuthenticatorName) {
        get("/games/{gameId}/followers") {
            gameController.handleGetFollowers(call)
        }
    }
}
