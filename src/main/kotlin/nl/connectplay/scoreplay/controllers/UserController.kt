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

    suspend fun handleListAsync(call: ApplicationCall) {
        // These are optional query parameters:
        // "limit" controls how many users to return (`/users?limit=10` returns up to 10 users)
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        // "offset" controls how many users to skip before starting to return results (`/users?offset=10` skips the first 10 users)
        val offset = call.request.queryParameters["offset"]?.toIntOrNull()
        // "query" is used for searching usernames (`/users?query=em` will match 'emma' and 'emre')
        val query = call.request.queryParameters["query"]

        try {
            // Get the users from the repository.
            // If no users are found (repository returns null), respond with 204 No Content.
            val users: List<UserDto> =
                userRepository.getUsersAsync(limit, offset, query) ?: return call.respond(HttpStatusCode.NoContent)

            // If users are found, respond with 200 OK and the list of users as JSON.
            call.respond(HttpStatusCode.OK, users)
        } catch (e: SQLException) {
            // Log the SQL error and respond with 500 Internal Server Error.
            call.application.environment.log.error("DB error while getting users", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleOneAsync(call: ApplicationCall) {
        // Try to read the "id" path parameter from the route (e.g. /users/5 → id = 5)
        // If it's missing or not a valid number, immediately respond with 400 Bad Request.
        val userId = call.parameters["id"]?.toIntOrNull() ?: return call.respond(
            HttpStatusCode.BadRequest, "{'message': 'User Id must be a number'}"
        )

        try {
            val user = userRepository.getUserByIdAsync(userId) ?: return call.respond(
                HttpStatusCode.NotFound, "User not found"
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