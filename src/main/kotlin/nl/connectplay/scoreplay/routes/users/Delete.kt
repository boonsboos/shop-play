package nl.connectplay.scoreplay.routes.users

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.deleteUserRoute() {
    val userController: UserController by inject() // with the injection can we get a instance of the userController by Koin dependency

    authenticate(UserIdJWTAuthenticatorName) {
        delete("/users/me") {
            userController.handleDeleteUserAsync(call)
        }
    }

}