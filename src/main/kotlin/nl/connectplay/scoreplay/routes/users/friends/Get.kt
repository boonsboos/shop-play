package nl.connectplay.scoreplay.routes.users.friends

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.getFriends() {
    val userController by inject<UserController>()

    get("/users/{id}/friends") {
        userController.handleGetFriendsForUserAsync(call)
    }
}