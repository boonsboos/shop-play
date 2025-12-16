package nl.connectplay.scoreplay.routes.users.picture

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.routes.ApiRoute
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.userUploadPictureRoute() {
    val userController by inject<UserController>()

    authenticate(UserIdJWTAuthenticatorName) {
        post("/users/me/picture") {
            userController.handleUploadPictureAsync(call)
        }
    }
}
