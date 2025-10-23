package nl.connectplay.scoreplay.routes.games

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamePostRoutes() {
    val gameController by inject<GameController>()

    authenticate(UserIdJWTAuthenticatorName) {
        post("/games") {
            gameController.handleCreateAsync(call)
        }

        post("/games/{gameId}/follow") {
            gameController.handleFollowGame(call)
        }
    }
}