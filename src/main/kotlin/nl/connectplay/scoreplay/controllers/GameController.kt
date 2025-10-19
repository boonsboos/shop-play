package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.CreateGameDto
import kotlinx.datetime.LocalDate
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

    suspend fun handleCreateAsync(call: ApplicationCall) {
        // Checks if required parameters are not missing
        val params = call.receive<Map<String, String>>()
        val name = params["name"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing name")
        val description = params["description"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing description")
        val publisher = params["publisher"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing publisher")

        val minPlayers = params["minPlayers"]?.toIntOrNull()
        val maxPlayers = params["maxPlayers"]?.toIntOrNull()
        val duration = params["duration"]?.toIntOrNull()
        val minAge = params["minAge"]?.toIntOrNull()
        val releaseDate = params["releaseDate"]?.let {
            try {
                LocalDate.parse(it)
            } catch (ex: Exception) {
                return call.respond(HttpStatusCode.BadRequest, "Invalid releaseDate format, use yyyy-MM-dd")
            }
        }

        // Creates a CreateGameDto
        val create = CreateGameDto(
            name = name,
            description = description,
            publisher = publisher,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            duration = duration,
            minAge = minAge,
            releaseDate = releaseDate
        )

        // Sends CreateGameDto to repository with HttpStatusCode 200 (OK)
        try {
            val created = gameRepository.addGame(create)
            call.respond(HttpStatusCode.Created, created)
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.Conflict, e.message ?: "Conflict")
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating game", e)
            call.respond(HttpStatusCode.InternalServerError)
        } catch (e: Exception) {
            call.application.environment.log.error("Error while creating game", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUpdateAsync(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Game Id must be a number")

        val params = call.receive<Map<String, String>>() // keep same style
        // parse optional fields
        val name = params["name"]
        val description = params["description"]
        val publisher = params["publisher"]
        val minPlayers = params["minPlayers"]?.toIntOrNull()
        val maxPlayers = params["maxPlayers"]?.toIntOrNull()
        val duration = params["duration"]?.toIntOrNull()
        val minAge = params["minAge"]?.toIntOrNull()
        val releaseDate = params["releaseDate"]?.let {
            try {
                LocalDate.parse(it) // yyyy-MM-dd
            } catch (ex: Exception) {
                return call.respond(HttpStatusCode.BadRequest, "Invalid releaseDate format, use yyyy-MM-dd")
            }
        }
        
        // Put parameters in UpdateGameDto
        val update = UpdateGameDto(
            name = name,
            description = description,
            publisher = publisher,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            duration = duration,
            minAge = minAge,
            releaseDate = releaseDate
        )

        try {
            val updated = gameRepository.updateGame(id, update)
                ?: return call.respond(HttpStatusCode.NotFound, "Game not found")

            call.respond(HttpStatusCode.OK, updated)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while updating game", e)
            call.respond(HttpStatusCode.InternalServerError)
        } catch (e: Exception) {
            call.application.environment.log.error("Error while updating game", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}
