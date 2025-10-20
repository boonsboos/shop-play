package nl.connectplay.scoreplay.routes.notifications

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.patchNotificationsRoute() {
    val notificationController by inject<NotificationController>()

    // PATCH /notifications - Mark notification as Read
    patch("/notifications/{id}") {
        notificationController.handleMarkAsReadAsync(call)
    }
}
