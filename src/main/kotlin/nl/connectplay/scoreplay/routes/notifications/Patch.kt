package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.patchNotificationRoute() {
    val notificationController: NotificationController by inject()

    authenticate(UserIdJWTAuthenticatorName) {
        patch("/notifications/{notificationId}"){
            notificationController.handleMarkAsReadAsync(call)
        }
    }
}


