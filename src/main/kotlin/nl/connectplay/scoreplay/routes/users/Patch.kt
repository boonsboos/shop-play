package nl.connectplay.scoreplay.routes.users

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.userPatchRoute() {
    val userController by inject<UserController>()

    authenticate(UserIdJWTAuthenticatorName) {
        patch("/users/{id}") {
            userController.handleUpdateUserAsync(call)  // call is a Ktor-object from the type ApplicationCall
        }
    }
}