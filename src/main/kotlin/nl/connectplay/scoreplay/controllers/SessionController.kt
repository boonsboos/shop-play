package nl.connectplay.scoreplay.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.data.DatabaseSessionRepository
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import java.sql.SQLException
import java.util.UUID

/**
 * Controller for managing CRUD operations on sessions
 */
class SessionController() {

    val repository: SessionRepository = DatabaseSessionRepository()

    suspend fun handleSessionCreation(call: ApplicationCall) {
        val body: CreateSessionDto? = call.receiveNullable<CreateSessionDto>()
        if (body == null) {
            return call.respond(HttpStatusCode.BadRequest)
        }

        try {
            val uuid: UUID? = repository.createSessionAsync(body)
            if (uuid == null) {
                return call.respond(HttpStatusCode.InternalServerError)
            }

            call.respond(HttpStatusCode.Created, uuid);
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating session", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}