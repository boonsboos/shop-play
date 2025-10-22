package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.FollowGameRepository
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.dto.CreateGameDto
import nl.connectplay.scoreplay.models.dto.UpdateGameDto
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

class GameController(
    private val gameRepository: GameRepository,
    private val followGameRepository: FollowGameRepository,
    private val pictureService: PictureService
) {

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
            ).all { it == null }
        ) {
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

    suspend fun handleFollowGame(call: ApplicationCall) {
        val gameId = call.parameters["gameId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid gameId")
        // check if the user is authorized
        val principal = call.principal<JWTPrincipal>() // get the user info from the JWT
        val userId = principal?.payload?. // get the payload from the JWT principal object
        getClaim("userId")?.asInt() // get the "userId" claim value from the payload as int
            ?: return call.respond(HttpStatusCode.Unauthorized, "User not authenticated") // code 401

        try {
            followGameRepository.followGame(userId, gameId)
            call.respond(HttpStatusCode.Created, "Successfully following the game.")
        } catch (e: SQLIntegrityConstraintViolationException) {
            call.respond(HttpStatusCode.Conflict, "User already follows this game")
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while following game", e)
            call.respond(HttpStatusCode.InternalServerError, "Database error")
        }
    }

    suspend fun handleUnfollowGame(call: ApplicationCall) {
        val gameId = call.parameters["gameId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid gameId")

        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload
            ?.getClaim("userId")?.asInt()
            ?: return call.respond(HttpStatusCode.Unauthorized, "User not authenticated")

        try {
            followGameRepository.unfollowGame(userId, gameId)
            call.respond(HttpStatusCode.OK, "Successfully unfollowed the game")
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while unfollowing game", e)
            call.respond(HttpStatusCode.InternalServerError, "Database error")
        }
    }

    suspend fun handleGetFollowers(call: ApplicationCall) {
        val gameId = call.parameters["gameId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid gameId")
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

        try {
            val followers = followGameRepository.getFollowers(gameId, offset, limit)
            call.respond(HttpStatusCode.OK, followers)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while getting followers", e)
            call.respond(HttpStatusCode.InternalServerError, "Database error")
        }
    }

    suspend fun handleUploadPictureAsync(call: ApplicationCall) {
        val sessionId = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")
        val contentType = call.request.contentType()

        when {
            contentType.match(ContentType.Application.Json) -> {
                val uploadPictures = call.receive<List<UploadPictureDto>>()

                if (uploadPictures.isEmpty()) return call.respond(HttpStatusCode.BadRequest, "No pictures provided")
                if (uploadPictures.size > 10) return call.respond(
                    HttpStatusCode.PayloadTooLarge,
                    "Too many pictures, max 10 pictures"
                )

                // Asynchronously upload each picture, because there is no bulk upload method (yet)
                val results = coroutineScope {
                    uploadPictures.mapIndexed { index, uploadPicture ->
                        async {
                            val (status, body) = pictureService.handleUploadImageJsonAsync(
                                uploadPicture,
                                PictureService.EntityType.Game,
                                sessionId,
                            )
                            mapOf(
                                "index" to index,
                                "status" to status.value,
                                "message" to body
                            )
                        }
                    }.awaitAll()
                }

                // If all succeeded (Created = 201), respond 201; else return 207 Multi-Status
                val overallStatus = if (results.all { it["status"] == HttpStatusCode.Created.value }) {
                    HttpStatusCode.Created
                } else {
                    HttpStatusCode.MultiStatus // partial success/failure
                }

                call.respond(overallStatus, results)
            }

            else -> {
                return call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported content type")
            }
        }
    }
}
