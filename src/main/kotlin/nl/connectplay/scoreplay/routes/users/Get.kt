package nl.connectplay.scoreplay.routes.users

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.usersRoute() {
    val userController = UserController()

    get("/users") {
        userController.handleListAsync(call)
    }

    get("/users/{id}") {
        userController.handleOneAsync(call)
    }
}
