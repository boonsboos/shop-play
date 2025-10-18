package nl.connectplay.scoreplay.controllers

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import java.sql.SQLException
import java.util.UUID

/**
 * Controller for managing CRUD operations on sessions
 */
class SessionController(
    private val repository: SessionRepository,
    private val pictureService: PictureService
) {

    suspend fun handleSessionCreation(call: ApplicationCall) {
        val body: CreateSessionDto = call.receiveNullable<CreateSessionDto>()
            ?: return call.respond(HttpStatusCode.BadRequest)

        try {
            val uuid: UUID = repository.createSessionAsync(body)
                ?: return call.respond(HttpStatusCode.InternalServerError)

            call.respond(HttpStatusCode.Created, uuid)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating session", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUploadPictureAsync(call: ApplicationCall) {
        val sessionId = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid session id")
        val contentType = call.request.contentType()

        when {
            contentType.match(ContentType.Application.Json) -> {
                val uploadPicture = call.receive<UploadPictureDto>()
                val res =
                    pictureService.handleUploadImageJsonAsync(
                        uploadPicture,
                        PictureService.EntityType.SESSION,
                        sessionId
                    )
                call.respond(res.first, res.second)
            }

            else -> {
                return call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported content type")
            }
        }
    }
}
