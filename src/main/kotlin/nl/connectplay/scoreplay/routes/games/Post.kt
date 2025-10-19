package nl.connectplay.scoreplay.routes.games

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesFollowRoute() {
    val gameController by inject<GameController>()

    authenticate(UserIdJWTAuthenticatorName) {
        post("/games/{gameId}/follow") {
            gameController.handleFollowGame(call)
        }
    }
}

