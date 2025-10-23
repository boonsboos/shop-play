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
fun Route.addFriend() {
    val userController by inject<UserController>()

    authenticate(UserIdJWTAuthenticatorName) {
        post("/users/{id}/friends") {
            try {
                if (call.getUserIdFromJWT() != call.parameters["id"]?.toInt())
                    return@post call.respond(HttpStatusCode.Forbidden)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while making a friend request: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized)
            }
            userController.handleNewFriendRequestAsync(call)
        }
    }
}