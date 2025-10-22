package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getUserRoute() {
    val notificationController: NotificationController by inject() // with the injection can we get an instance of the userController by Koin dependency
    authenticate(UserIdJWTAuthenticatorName) {
    // GET /notifications/{notificationId} — Get notification by ID
    get("/notifications/{notificationId}") {
        notificationController.handleGetNotificationById(call)
    }

    // GET /notifications — List of notifications by user ID
    get("/notifications") {
        notificationController.handleGetAllNotifications(call)
        }
    }
}
