package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import nl.connectplay.scoreplay.data.DatabaseUserRepository
import nl.connectplay.scoreplay.models.dto.UserDto
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import java.sql.SQLException

class UserController {
    val userRepository = DatabaseUserRepository()

    suspend fun handleAsync(call: ApplicationCall) {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val offset = call.request.queryParameters["offset"]?.toIntOrNull()
        val query = call.request.queryParameters["query"]

        try {
            val users: List<UserDto> =
                userRepository.getUsersAsync(limit, offset, query) ?: return call.respond(HttpStatusCode.NoContent)

            call.respond(HttpStatusCode.OK, users)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while creating session", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleOneAsync(call: ApplicationCall) {
        // get userid from /users/{id}
        val userId = call.parameters["id"]?.toIntOrNull() ?: return call.respond(
            HttpStatusCode.BadRequest,
            "User Id must be a number"
        )

        try {
            val user = userRepository.getUserByIdAsync(userId) ?: return call.respond(
                HttpStatusCode.NotFound,
                "User not found"
            )

            call.respond(HttpStatusCode.OK, user)
        } catch (e: NullPointerException) {
        }
    }

    suspend fun handleRegisterAsync(call: ApplicationCall) {
        val params = call.receive<Map<String, String>>() // read the JSON body as key-value pairs
        val username = params["username"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing username")
        val email = params["email"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing email")
        val password = params["password"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing password")
        val user = CreateUserDto(username, email, password) // create new User object

        try {
            userRepository.addUser(user) // try to save new user
            call.respond(HttpStatusCode.Created, user) // send the 201 code as text and the data of the user
        } catch (e: IllegalArgumentException) { // catch the Exception from the UserRepository
            // handle duplicate or invalid user data
            call.respond(HttpStatusCode.Conflict, e.message ?: "User already exists")
        } catch (e: SQLException) {
            // handle unexpected database errors
            call.application.environment.log.error("DB error while adding user", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}