package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.response.respond
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import java.sql.SQLException

class GameController(
    private val gameRepository: GameRepository,
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
                                sessionId
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
