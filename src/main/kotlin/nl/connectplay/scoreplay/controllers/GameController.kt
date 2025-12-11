package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.connectplay.scoreplay.abstraction.data.FollowGameRepository
import nl.connectplay.scoreplay.abstraction.data.GamePictureRepository
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.abstraction.services.CdnService
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.Game
import nl.connectplay.scoreplay.models.dto.game.CreateGameDto
import nl.connectplay.scoreplay.models.dto.game.GameDto
import nl.connectplay.scoreplay.models.dto.game.UpdateGameDto
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import nl.connectplay.scoreplay.utilities.getLimitQueryParameter
import nl.connectplay.scoreplay.utilities.getOffsetQueryParameter
import nl.connectplay.scoreplay.utilities.getSearchQueryParameter
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

class GameController(
    private val gameRepository: GameRepository,
    private val followGameRepository: FollowGameRepository,
    private val pictureService: PictureService,
    private val gamePictureRepository: GamePictureRepository,
    private val cdnService: CdnService
) {
    suspend fun handleListAsync(call: ApplicationCall) {
        val limit = call.request.getLimitQueryParameter()
        val offset = call.request.getOffsetQueryParameter()
        val query = call.request.getSearchQueryParameter()
        // Try getting all games from db shows NoContent if empty or InternalServerError if something went wrong in catch
        try {
            val games = gameRepository.getGamesAsync(limit, offset, query).asFlow().map { game ->
                val pictures = gamePictureRepository.getGamePictureUrls(game.id).firstOrNull()

                game.withPictures(listOfNotNull(pictures))
            }.toList()
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
                ?: return call.respond(HttpStatusCode.InternalServerError, "Try again later")

            call.respond<Game>(HttpStatusCode.Created, created)
        } catch (e: SQLIntegrityConstraintViolationException) {
            call.application.environment.log.error("Conflict while creating new game", e)
            call.respond(HttpStatusCode.Conflict)
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
            call.respond(HttpStatusCode.MultiStatus, "User already follows this game or game does not exist")
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while following game", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUnfollowGame(call: ApplicationCall) {
        val gameId = call.parameters["gameId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid gameId")

        val userId = call.getUserIdFromJWT()

        try {
            followGameRepository.unfollowGame(userId, gameId)
            call.respond(HttpStatusCode.OK, "Successfully unfollowed the game")
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while unfollowing game", e)
            call.respond(HttpStatusCode.InternalServerError)
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
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUploadPictureAsync(call: ApplicationCall) {
        val gameId = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid game id")
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
                                PictureService.EntityType.GAME,
                                gameId
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

            contentType.match(ContentType.MultiPart.FormData) -> {
                val parts = call.receiveMultipart()

                val formData = parts.readPart() ?: return call.respond(HttpStatusCode.BadRequest)
                if ((formData.contentType != ContentType.Image.JPEG && formData.contentType != ContentType.Image.PNG) || formData !is PartData.FileItem) {
                    return call.respond(HttpStatusCode.BadRequest, "Please upload PNG or JPEG images only")
                }

                val response = try {
                    cdnService.forwardImage(formData)
                } catch (e: Exception) {
                    // always log if anything goes wrong during the upload
                    call.application.environment.log.error("CDN error while uploading image", e)
                    return call.respond(HttpStatusCode.InternalServerError)
                } finally {
                    // always dispose the rest of the form data parts after forwarding the image to the CDN
                    parts.forEachPart { part -> part.dispose() }
                }

                if (response.status != HttpStatusCode.OK) {
                    call.application.environment.log.warn("Failed to upload picture: ${response.status}")
                    return call.respond(HttpStatusCode.BadRequest, "Failed to upload picture")
                }

                val pictureUrl = "https://api.connect-en-play.nl/images/${response.headers["Location"]}"

                saveUrl(pictureUrl, gameId, call)
            }

            else -> {
                return call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported content type")
            }
        }
    }

    private suspend fun saveUrl(
        pictureUrl: String,
        gameId: String,
        call: ApplicationCall
    ) {
        try {
            return if (pictureService.uploadImageByUrlAsync(
                    pictureUrl,
                    PictureService.EntityType.GAME,
                    gameId
                )
            ) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Failed to upload picture")
            }
        } catch (e: SQLIntegrityConstraintViolationException) {
            call.application.environment.log.warn("Encountered duplicate while uploading image URL $pictureUrl")
            return call.respond(HttpStatusCode.Conflict)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while uploading picture", e)
            return call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleSingleAsync(call: ApplicationCall) {
        val gameId = call.parameters["gameId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid game id")

        try {
            val game = gameRepository.getGameByIdAsync(gameId) ?: return call.respond(HttpStatusCode.NotFound)

            val pictures = gamePictureRepository.getGamePictureUrls(gameId)
            return call.respond<GameDto>(status = HttpStatusCode.OK, message = game.withPictures(pictures))
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while fetching game", e)
            return call.respond(HttpStatusCode.InternalServerError)
        }
    }
}