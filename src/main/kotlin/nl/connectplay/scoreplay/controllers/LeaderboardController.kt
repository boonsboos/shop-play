package nl.connectplay.scoreplay.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.data.LeaderboardRepository
import java.sql.SQLException

class LeaderboardController(private val leaderboardRepository: LeaderboardRepository) {
    suspend fun handleGetTopScoresForGame(call: ApplicationCall) {
        val gameId = call.parameters["gameId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid gameId")

        try {
            val topScores = leaderboardRepository.getTopScoresForGame(gameId)
            // because the function getTopScoresForGame() always returns a list we need to check if the list is empty
            if (topScores.isEmpty()) return call.respond(HttpStatusCode.NotFound, "No scores found for game $gameId")
            call.respond(HttpStatusCode.OK, topScores)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while receving leaderboard", e)
            call.respond(HttpStatusCode.InternalServerError, "Database error")
        }
    }
}