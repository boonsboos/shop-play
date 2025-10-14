package nl.connectplay.scoreplay.routes.users

import io.ktor.server.routing.*
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.usersRegisterRoute() {
    val userController by inject<UserController>()

    post("/register") {
        userController.handleRegisterAsync(call) // call is a Ktor-object from the type ApplicationCall
    }

    post("/login") {
        userController.handleLoginAsync(call)
    }
}

