package nl.connectplay.scoreplay.routes.users

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.usersRoute() {
    val userController by inject<UserController>()

    authenticate(UserIdJWTAuthenticatorName) {
        get("/users") {
            userController.handleListAsync(call)
        }

        get("/users/{id}") {
            userController.handleOneAsync(call)
        }

        get("/users/{id}/followed") {
            userController.handleFollowedGamesAsync(call)
        }

        route("/users/me") {
            get{
                userController.handleMeAsync(call)
            }

            get("/friendrequests") {
                userController.handleGetFriendRequestsForUserAsync(call)
            }
        }
    }
}
