package nl.connectplay.scoreplay.routes.users

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.usersRoute() {
    val usersController = UserController()

    get("/users") {
        // delegate handling this call to the ExampleController
        usersController.handleAsync(call) // call is an implicit variable referring to the HTTP call
    }
}
