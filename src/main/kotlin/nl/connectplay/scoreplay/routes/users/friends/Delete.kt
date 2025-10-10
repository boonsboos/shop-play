package nl.connectplay.scoreplay.routes.users.friends

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.getValue

@ApiRoute
fun Route.removeFriend() {
    val userController by inject<UserController>()

    delete("/users/{id}/friends/{friendId}") {
        userController.handlePatchFriendRequest(call)
    }
}