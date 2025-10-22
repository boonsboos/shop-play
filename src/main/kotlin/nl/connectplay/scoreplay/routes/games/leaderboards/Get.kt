package nl.connectplay.scoreplay.routes.games.leaderboards

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.LeaderboardController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject

@ApiRoute
fun Route.leaderboardRoute() {
    val leaderboardController by inject<LeaderboardController>()

    get("/games/{gameId}/leaderboard") {
        leaderboardController.handleGetTopScoresForGame(call)
    }
}
