package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.patchUserRoute() {
    val notificationController: NotificationController by inject() // with the injection can we get an instance of the userController by Koin dependency

    // PATCH /notifications/{notificationId} Mark notification as read
    get("/notifications/{notificationId}") patch@{
        notificationController.handleMarkAsReadAsync(call)
    }
}


