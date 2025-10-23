package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.deleteNotificationRoute() {
    val notificationController: NotificationController by inject()

    authenticate(UserIdJWTAuthenticatorName) {
        delete("/notifications/{notificationId}") {
            notificationController.handleDeleteNotificationAsync(call)
        }
    }
}


