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
fun Route.getUserRoute() {
    val notificationController: NotificationController by inject() // with the injection can we get an instance of the userController by Koin dependency
    authenticate(UserIdJWTAuthenticatorName) {
        // GET /notifications — Get notification by ID
        get("/notifications/{id}") {
            try {
                if(call.getUserIdFromJWT() != call.parameters["id"]?.toInt())
                    return@get call.respond(HttpStatusCode.Forbidden)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while getting notification: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized)
            }

            notificationController.handleGetNotificationById(call)
        }
        // GET /notifications — List of notifications by user ID
        get("/notifications/{id}") {
            try {
                if(call.getUserIdFromJWT() != call.parameters["id"]?.toInt())
                    return@get call.respond(HttpStatusCode.Forbidden)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while getting notifications: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized)
            }

            notificationController.handleListByUserIdAsync(call)
            }
        // GET /notifications - List of all notifications
        get("/notifications") {
            try {
                if(call.getUserIdFromJWT() != call.parameters["id"]?.toInt())
                    return@get call.respond(HttpStatusCode.Forbidden)
            } catch (e: UnauthorizedException) {
                call.application.environment.log.error("Authorization error while getting notifications: ${e.message}")
                call.respond(HttpStatusCode.Unauthorized)
            }

            notificationController.handleGetAllNotifications(call)
        }
    }

}
