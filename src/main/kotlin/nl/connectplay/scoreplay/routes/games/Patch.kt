package nl.connectplay.scoreplay.routes.games

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.gamesUpdateRoute() {
    val gameController by inject<GameController>()

    authenticate (UserIdJWTAuthenticatorName) {
        patch("/games/{id}") {
            gameController.handleUpdateAsync(call)
        }
    }
}
