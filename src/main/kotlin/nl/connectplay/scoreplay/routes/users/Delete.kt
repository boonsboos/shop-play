package nl.connectplay.scoreplay.routes.users

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
fun Route.deleteUserRoute() {
    val userController: UserController by inject() // with the injection can we get a instance of the userController by Koin dependency

    authenticate(UserIdJWTAuthenticatorName) {
        delete("/users/{id}") {
            try {
                if (call.getUserIdFromJWT() != call.parameters["id"]?.toInt())
                    return@delete call.respond(HttpStatusCode.Forbidden)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while deleting user: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized)
            }

            userController.handleDeleteUserAsync(call)
        }
    }

}