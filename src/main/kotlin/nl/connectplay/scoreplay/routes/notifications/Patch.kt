package nl.connectplay.scoreplay.routes.notifications

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.routes.ApiRoute
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.patchUserRoute() {
    val notificationController: NotificationController by inject() // with the injection can we get an instance of the userController by Koin dependency
    authenticate(UserIdJWTAuthenticatorName) {
        get("/notifications/{id}") patch@{
            try {
                if(call.getUserIdFromJWT() != call.parameters["id"]?.toInt())
                    return@patch call.respond(HttpStatusCode.Forbidden)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while deleting user: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized)
            }

            notificationController.handleMarkAsReadAsync(call)
        }
    }

}
