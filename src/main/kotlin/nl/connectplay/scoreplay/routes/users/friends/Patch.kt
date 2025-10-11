package nl.connectplay.scoreplay.routes.users.friends

import io.ktor.server.routing.Route
import io.ktor.server.routing.patch
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.getValue

@ApiRoute
fun Route.updateFriendRequest() {
    val userController by inject<UserController>()

    patch("/users/{id}/friends/{friendId}") {
        userController.handlePatchFriendRequest(call)
    }
}