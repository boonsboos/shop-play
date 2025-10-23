package nl.connectplay.scoreplay.routes.games.pictures

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.getValue

@ApiRoute
fun Route.sessionUploadPictureRoute() {
    val gameController by inject<GameController>()

    authenticate(UserIdJWTAuthenticatorName) {
        patch("/games/{id}/pictures") {
            gameController.handleUploadPictureAsync(call)
        }
    }
}
