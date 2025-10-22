package nl.connectplay.scoreplay.routes.users.picture

import io.ktor.server.routing.Route
import io.ktor.server.routing.patch
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.userUploadPictureRoute() {
    val userController by inject<UserController>()

    patch("/users/{id}/picture") {
        userController.handleUploadPictureAsync(call)
    }
}
