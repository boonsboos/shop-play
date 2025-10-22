package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.deleteUserRoute() {
    val notificationController: NotificationController by inject() // with the injection can we get an instance of the userController by Koin dependency
    authenticate(UserIdJWTAuthenticatorName) {
        // DELETE /notifications/{notificationId} Delete a notification
        delete("/notifications/{notificationId}") {
            notificationController.handleDeleteNotificationAsync(call)
        }
    }
}


