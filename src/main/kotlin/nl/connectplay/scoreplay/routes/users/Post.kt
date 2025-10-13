package nl.connectplay.scoreplay.routes.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.routing.post // import the post function to handle post request
import io.ktor.server.routing.Route
import nl.connectplay.scoreplay.UserIdJWTClaim
import nl.connectplay.scoreplay.controllers.UserController // gives access to the UserController class
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import java.util.Date
import kotlin.getValue

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

