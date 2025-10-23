package nl.connectplay.scoreplay.routes.users.friends

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
fun Route.removeFriend() {
    val userController by inject<UserController>()

    authenticate(UserIdJWTAuthenticatorName) {
        delete("/users/{id}/friends/{friendId}") {
            try {
                if (call.parameters["id"]?.toInt() != call.getUserIdFromJWT()) {
                    return@delete call.respond(HttpStatusCode.Forbidden)
                }
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while removing user as friend: ${e.message}")
            }

            userController.handleDeleteFriend(call)
        }
    }
}