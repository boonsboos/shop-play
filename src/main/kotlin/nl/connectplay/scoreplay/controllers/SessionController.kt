package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.services.FriendService
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.abstraction.services.SessionService
import nl.connectplay.scoreplay.models.SessionVisibility
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import nl.connectplay.scoreplay.models.dto.session.UpdateSessionDto
import nl.connectplay.scoreplay.utilities.getLimitQueryParameter
import nl.connectplay.scoreplay.utilities.getOffsetQueryParameter
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import java.sql.SQLException
import java.util.*

/**
 * Controller for managing CRUD operations on sessions
 */
class SessionController(
    private val repository: SessionRepository,
    private val pictureService: PictureService,
    private val friendService: FriendService,
    private val sessionService: SessionService
) {

    suspend fun handleSessionCreation(call: ApplicationCall) {
        val body: CreateSessionDto =
            call.receiveNullable<CreateSessionDto>() ?: return call.respond(HttpStatusCode.BadRequest)

        try {
            val uuid: UUID =
                repository.createSessionAsync(body) ?: return call.respond(HttpStatusCode.InternalServerError)

            call.respond(HttpStatusCode.Created, uuid)
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
                val res = pictureService.handleUploadImageJsonAsync(
                    uploadPicture, PictureService.EntityType.SESSION, sessionId, userId
                )
                call.respond(res.first, res.second)
            }

            else -> {
                return call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported content type")
            }
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
        val sessionId = call.parameters["sessionId"]
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")

        val (status, message) = sessionService.getSessionAsync(authedUserId, targetUserId, UUID.fromString(sessionId))
        return call.respondNullable(status = status, message = message)
    }

    suspend fun handleUpdateSessionAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = UUID.fromString(
            call.parameters["sessionId"]
                ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")
        )
        var updateSession = call.receiveNullable<UpdateSessionDto>()
            ?: return call.respond(HttpStatusCode.BadRequest, "Body is incorrect or empty")

        try {
            val existingSession = repository.getSessionByIdAsync(sessionId, userId)
                ?: return call.respond(HttpStatusCode.NotFound, "Session not found")
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
}
