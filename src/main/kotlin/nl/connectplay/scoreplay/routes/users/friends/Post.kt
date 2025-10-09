package nl.connectplay.scoreplay.routes.users.friends

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.addFriend() {
    val userController by inject<UserController>()

    post("/users/{userId}/friends") {
        userController.handleNewFriendRequestAsync(call)
    }
}