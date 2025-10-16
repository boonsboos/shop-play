package nl.connectplay.scoreplay.routes.games.picture

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.getValue

@ApiRoute
fun Route.sessionUploadPictureRoute() {
    val gameController by inject<GameController>()

    patch("/games/{id}/picture") {
        println("Patch session picture")
//        gameController.handleUploadPictureAsync(call)
    }
}
