package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getNotificationRoutes() {
    val notificationController: NotificationController by inject()

    authenticate(UserIdJWTAuthenticatorName) {
        get("/notifications/{notificationId}") {
            notificationController.handleGetNotificationById(call)
        }

        get("/notifications") {
            notificationController.handleGetAllNotifications(call)
        }
    }
}
