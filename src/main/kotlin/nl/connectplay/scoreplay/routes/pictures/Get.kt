package nl.connectplay.scoreplay.routes.pictures

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.PictureController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getPictures() {
    val pictureController by inject<PictureController>()

    authenticate(UserIdJWTAuthenticatorName) {
        get("/pictures/{id}") {
            pictureController.handleGetPictureAsync(call)
        }
    }
}