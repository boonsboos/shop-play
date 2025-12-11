package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.services.CdnService
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.abstraction.services.SessionService
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import nl.connectplay.scoreplay.models.dto.session.UpdateSessionDto
import nl.connectplay.scoreplay.utilities.getLimitQueryParameter
import nl.connectplay.scoreplay.utilities.getOffsetQueryParameter
import nl.connectplay.scoreplay.utilities.getUUIDOrNull
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

/**
 * Controller for managing CRUD operations on sessions
 */
class SessionController(
    private val repository: SessionRepository,
    private val pictureService: PictureService,
    private val cdnService: CdnService,
    private val sessionService: SessionService
) {

    suspend fun handleSessionCreation(call: ApplicationCall) {
        val body: CreateSessionDto =
            call.receiveNullable<CreateSessionDto>() ?: return call.respond(
                HttpStatusCode.BadRequest,
                "Invalid request body"
            )

        try {
            val uuid: UUID =
                repository.createSessionAsync(body) ?: return call.respond(HttpStatusCode.InternalServerError)

            call.respond(HttpStatusCode.Created, uuid.toString())
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating session", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUploadPictureAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = call.parameters["id"] ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")
        val contentType = call.request.contentType()

        when {
            contentType.match(ContentType.Application.Json) -> {
                val uploadPicture = call.receive<UploadPictureDto>()

                try {
                    val res = pictureService.handleUploadImageJsonAsync(
                        uploadPicture, PictureService.EntityType.SESSION, sessionId, userId
                    )
                    call.respond(res.first, res.second)
                } catch (e: UnauthorizedException) {
                    call.application.environment.log.error("User $userId tried to upload picture to session $sessionId but they are not the host")
                    call.respond(HttpStatusCode.Forbidden, "You are not the host")
                }
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

                saveUrl(pictureUrl, sessionId, call)
            }

            else -> {
                return call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported content type")
            }
        }
    }

    private suspend fun saveUrl(
        pictureUrl: String,
        sessionId: String,
        call: ApplicationCall
    ) {
        try {
            return if (pictureService.uploadImageByUrlAsync(
                    pictureUrl,
                    PictureService.EntityType.SESSION,
                    sessionId
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

    suspend fun handleListAsync(call: ApplicationCall) {
        val authedUserId = call.getUserIdFromJWT()// id of the authenticated user (from the JWT)
        val targetUserId = call.parameters["targetId"]?.toInt() // id of the user whose sessions are being requested
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid user id")
        val limit = call.request.getLimitQueryParameter()
        val offset = call.request.getOffsetQueryParameter()

        val (status, message) = sessionService.getSessionsAsync(authedUserId, targetUserId, limit, offset)
        return call.respondNullable(status = status, message = message)
    }

    suspend fun handleOneAsync(call: ApplicationCall) {
        val authedUserId = call.getUserIdFromJWT()// id of the authenticated user (from the JWT)
        val targetUserId = call.parameters["targetId"]?.toInt() // id of the user whose sessions are being requested
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid user id")
        val sessionId = call.getUUIDOrNull("sessionId")
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")

        val (status, message) = sessionService.getSessionAsync(authedUserId, targetUserId, sessionId)
        return call.respondNullable(status = status, message = message)
    }

    suspend fun handleUpdateSessionAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = call.getUUIDOrNull("sessionId")
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")

        var updateSession = call.receiveNullable<UpdateSessionDto>()
            ?: return call.respond(HttpStatusCode.BadRequest, "Body is incorrect or empty")

        try {
            val existingSession = repository.getSessionByIdAsync(sessionId)
                ?: return call.respond(HttpStatusCode.NotFound, "Session not found")

            if (existingSession.hostId != userId) {
                return call.respond(HttpStatusCode.Forbidden, "You are not the host")
            }

            // check if session already has an endTime, if so remove it from the UpdateSessionDto
            if (existingSession.endTime != null) {
                updateSession = UpdateSessionDto(endTime = null, visibility = updateSession.visibility) // Omit endTime
            }

            val success = repository.updateSessionAsync(sessionId, userId, updateSession)
            return if (success) {
                call.respond(HttpStatusCode.OK, "Session updated successfully")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Could not update session")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return call.respond(HttpStatusCode.InternalServerError, "Something went wrong while updating session")
        }
    }

    suspend fun handleDeleteSessionAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = UUID.fromString(
            call.parameters["sessionId"]
                ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")
        )

        try {
            val success = repository.deleteSessionAsync(userId, sessionId)
            if (success) {
                call.respond(HttpStatusCode.OK, "Session deleted successfully")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Could not delete session")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return call.respond(HttpStatusCode.InternalServerError, "Could not delete session")
        }
    }
}
