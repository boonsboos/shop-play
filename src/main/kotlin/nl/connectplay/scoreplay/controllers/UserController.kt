package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.data.DatabaseUserRepository
import nl.connectplay.scoreplay.models.dto.UserDto
import java.sql.SQLException

class UserController {
    val usersRepository = DatabaseUserRepository()

    suspend fun handleAsync(call: ApplicationCall) {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val offset = call.request.queryParameters["offset"]?.toIntOrNull()
        val query = call.request.queryParameters["query"]

        try {
            val users:List<UserDto>? = usersRepository.getUsersAsync(limit, offset, query)
            if (users == null) {
                return call.respond(HttpStatusCode.NoContent)
            }

            call.respond(
                HttpStatusCode.OK,
                users,
            )
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating session", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}