package nl.connectplay.scoreplay.routes.scores

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.NotificationController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getNotificationsRoute() {
    val notificationController by inject<NotificationController>()

    // GET /notifications — List of notifications by user ID
    get ("/notification/{id}") {
        notificationController.handleListByUserIdAsync(call)
    }
}