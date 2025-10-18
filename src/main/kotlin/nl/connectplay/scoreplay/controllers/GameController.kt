package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import java.sql.SQLException

class GameController(private val gameRepository: GameRepository) {

    suspend fun handleListAsync(call: ApplicationCall) {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val offset = call.request.queryParameters["offset"]?.toIntOrNull()
        val query = call.request.queryParameters["query"]
        // Try getting all games from db shows NoContent if empty or InternalServerError if something went wrong in catch
        try {
            val games = gameRepository.getGamesAsync(limit, offset, query)
                ?: return call.respond(HttpStatusCode.NoContent)
            call.respond(HttpStatusCode.OK, games)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while getting games", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
