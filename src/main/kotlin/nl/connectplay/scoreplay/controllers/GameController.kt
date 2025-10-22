package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.CreateGameDto
import nl.connectplay.scoreplay.models.dto.UpdateGameDto
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

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

    suspend fun handleCreateAsync(call: ApplicationCall) {
        // Receive CreateGameDto
        val createReq = call.receive<CreateGameDto>()

        // Validate required fields
        if (createReq.name.isBlank()) return call.respond(HttpStatusCode.BadRequest, "Missing name")
        if (createReq.description.isBlank()) return call.respond(HttpStatusCode.BadRequest, "Missing description")
        if (createReq.publisher.isBlank()) return call.respond(HttpStatusCode.BadRequest, "Missing publisher")

        try {
            // Pass DTO to repository
            val created = gameRepository.addGame(createReq)
            call.respond(HttpStatusCode.Created, created)
        } catch (e: SQLIntegrityConstraintViolationException) {
            call.application.environment.log.error("Conflict", e)
            call.respond(HttpStatusCode.Conflict, e.message ?: "Conflict")
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating game", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUpdateAsync(call: ApplicationCall) {
        // Check if given id is an Int
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Game Id must be a number")

        // Receive UpdateGameDto
        val updateReq = call.receive<UpdateGameDto>()
        
        // Checks if anything needs to update
        if (listOf(
                updateReq.name,
                updateReq.description,
                updateReq.publisher,
                updateReq.minPlayers,
                updateReq.maxPlayers,
                updateReq.duration,
                updateReq.minAge,
                updateReq.releaseDate
            ).all { it == null }) {
            return call.respond(HttpStatusCode.BadRequest, "No fields to update")
        }

        try {
            val updated = gameRepository.updateGame(id, updateReq)
                ?: return call.respond(HttpStatusCode.NotFound, "Game not found")
            call.respond(HttpStatusCode.OK, updated)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while updating game", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
