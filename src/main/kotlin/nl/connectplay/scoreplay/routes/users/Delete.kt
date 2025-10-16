package nl.connectplay.scoreplay.routes.users

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.deleteUserRoute() {
    val userController: UserController by inject() // with the injection can we get a instance of the userController by Koin dependency
    delete("/users/{id}") {
        userController.handleDeleteUserAsync(call)
    }
}