package nl.connectplay.scoreplay.routes.users.friends

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getFriends() {
    val userController by inject<UserController>()

    authenticate(UserIdJWTAuthenticatorName) {
        get("/users/{id}/friends") {
            userController.handleGetFriendsForUserAsync(call)
        }
    }
}