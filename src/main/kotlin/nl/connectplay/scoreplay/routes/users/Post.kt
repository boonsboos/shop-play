package nl.connectplay.scoreplay.routes.users

import io.ktor.server.routing.post // import the post function to handle post request
import io.ktor.server.routing.Route
import nl.connectplay.scoreplay.controllers.UserController // gives access to the UserController class
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.usersRegisterRoute() {
    val userController = UserController()

    post("/register") {
        userController.handleRegisterAsync(call) // call is a Ktor-object from the type ApplicationCall
    }
}

