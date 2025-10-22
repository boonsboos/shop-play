package nl.connectplay.scoreplay.routes.games

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.sse
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import nl.connectplay.scoreplay.UserIdJWTAuthenticatorName
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.seconds

@ApiRoute
fun Route.deleteFollowGameRoute() {
    val gameController by inject<GameController>()

    authenticate(UserIdJWTAuthenticatorName) {
        delete("/games/{gameId}/unfollow") {
            gameController.handleUnfollowGame(call)
        }
    }
}