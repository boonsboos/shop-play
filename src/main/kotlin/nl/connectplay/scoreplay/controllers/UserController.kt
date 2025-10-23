package nl.connectplay.scoreplay.controllers

import com.auth0.jwt.exceptions.JWTCreationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.logging.*
import nl.connectplay.scoreplay.abstraction.data.FollowGameRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.abstraction.services.FriendService
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.abstraction.services.UserAccountService
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.models.dto.friend.FriendRequestReplyDto
import nl.connectplay.scoreplay.models.dto.friend.FriendRequestResponseDto
import nl.connectplay.scoreplay.models.dto.friend.NewFriendRequestDto
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import nl.connectplay.scoreplay.models.dto.user.CreateUserDto
import nl.connectplay.scoreplay.models.dto.user.LoginUserDto
import nl.connectplay.scoreplay.models.dto.user.UserDto
import nl.connectplay.scoreplay.models.dto.user.UserUpdateDto
import nl.connectplay.scoreplay.utilities.getLimitQueryParameter
import nl.connectplay.scoreplay.utilities.getOffsetQueryParameter
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException

class UserController(
    private val userRepository: UserRepository,
    private val friendService: FriendService,
    private val userAccountService: UserAccountService,
    private val followGameRepository: FollowGameRepository,
    private val pictureService: PictureService
) {

    private val logger = LoggerFactory.getLogger(UserController::class.java)

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

        val user = userRepository.getUserByIdAsync(userId) ?: return call.respond(
            HttpStatusCode.NotFound, "User not found"
        )

        call.respond(HttpStatusCode.OK, user)
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
        } catch (_: SQLIntegrityConstraintViolationException) { // catch the Exception from the UserRepository
            // handle duplicate or invalid user data
            call.respond(HttpStatusCode.Conflict, "User already exists")
        } catch (e: SQLException) {
            // handle unexpected database errors
            logger.error("DB error while adding user", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleLoginAsync(call: ApplicationCall) {
        val loginDto = call.receiveNullable<LoginUserDto>()
            ?: return call.respond(HttpStatusCode.BadRequest, "Missing login")

        try {
            val token = userAccountService.loginAsync(loginDto)
            // respond with token in json
            call.respond(HttpStatusCode.OK, hashMapOf("token" to token))
        } catch (e: NotFoundException) {
            call.application.environment.log.error(e)
            call.respond(HttpStatusCode.Unauthorized) // we can pretend login fails if the user does not exist
        } catch (e: UnauthorizedException) {
            call.application.environment.log.error(e)
            call.respond(HttpStatusCode.Unauthorized) // user is not allowed if the passwords do not match
        } catch (e: SQLException) {
            call.application.environment.log.error(
                "DB error while logging in user '${loginDto.username ?: loginDto.email}'",
                e
            )
            call.respond(HttpStatusCode.InternalServerError)
        } catch (e: JWTCreationException) {
            call.application.environment.log.error("Failed to create JWT", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleNewFriendRequestAsync(call: ApplicationCall) {
        val requestBody = call.receiveNullable<NewFriendRequestDto>() ?: return call.respond(HttpStatusCode.BadRequest)
        val userId = call.parameters["id"]?.toIntOrNull() ?: return call.respond(HttpStatusCode.BadRequest)

        try {
            // are the users friends already?
            val areFriends = friendService.isFriendsAsync(userId, requestBody.friendId) ?: return call.respond(
                HttpStatusCode.InternalServerError
            ) // we failed to do a very important check
            if (areFriends) {
                return call.respond(HttpStatusCode.Conflict, "Already friends")
            }

            // create a friend request
            val friendRequestStatus = friendService.requestFriendAsync(userId, requestBody.friendId)
                ?: return call.respond(HttpStatusCode.InternalServerError) // we failed to create the request because of other errors

            // we respond with either friends
            call.respond(HttpStatusCode.Created, FriendRequestResponseDto(requestBody.friendId, friendRequestStatus))
        } catch (exception: IllegalStateException) {
            return call.respond(HttpStatusCode.Conflict, "Request already sent")
        } catch (exception: SQLException) {
            call.application.environment.log.error(
                "DB error while adding friend request to ${requestBody.friendId} for user $userId", exception
            )
            return call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleGetFriendsForUserAsync(call: ApplicationCall) {
        val userId = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest) // we can't continue without the user ID

        // use custom extension functions to get the query parameters to reduce code duplication
        val limit = call.request.getLimitQueryParameter()
        val offset = call.request.getOffsetQueryParameter()

        // not friends or not the actual user
        if (!(userId == call.getUserIdFromJWT() ||
                    friendService.isFriendsAsync(call.getUserIdFromJWT(), userId) == true)
        ) {
            return call.respond(HttpStatusCode.Forbidden)
        }

        // get all friends of the user
        try {
            val friendsAsUsers = friendService.getFriendsAsync(userId, limit, offset) ?: return call.respond(
                HttpStatusCode.InternalServerError
            ) // we failed to fetch all users

            call.respond(HttpStatusCode.OK, friendsAsUsers)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while getting friends for user $userId", e)
            call.respond(HttpStatusCode.InternalServerError) // we failed to fetch all users
        }
    }

    suspend fun handlePatchFriendRequest(call: ApplicationCall) {
        val userId = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest) // user ID is required
        val friendId = call.parameters["friendId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest) // friend ID is required

        // if the user calls the endpoint without specifying the parameter
        // we assume they do not want to accept
        val reply = call.receive<FriendRequestReplyDto>()

        try {
            when (reply.accept) {
                true -> friendService.acceptFriendAsync(userId, friendId)
                false -> friendService.rejectFriendAsync(userId, friendId)
            }

            return call.respond(HttpStatusCode.OK)
        } catch (sqlException: SQLException) {
            call.application.environment.log.error(
                "DB error while user $userId was replying to friend request from user $friendId", sqlException
            )
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleDeleteFriend(call: ApplicationCall) {
        val userId = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest) // user ID is required
        val friendId = call.parameters["friendId"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest) // friend ID is required

        try {
            friendService.removeFriendAsync(userId, friendId)

            return call.respond(HttpStatusCode.NoContent)
        } catch (sqlException: SQLException) {
            call.application.environment.log.error(
                "DB error while user $userId was unfriending user $friendId", sqlException
            )
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUploadPictureAsync(call: ApplicationCall) {
        val userId = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest, "Invalid user id")

        val contentType = call.request.contentType()

        when {
            contentType.match(ContentType.Application.Json) -> {
                val uploadPicture = call.receive<UploadPictureDto>()
                val res =
                    pictureService.handleUploadImageJsonAsync(uploadPicture, PictureService.EntityType.USER, userId)
                call.respond(res.first, res.second)
            }

            else -> {
                return call.respond(HttpStatusCode.UnsupportedMediaType, "Unsupported content type")
            }
        }
    }

    suspend fun handleUpdateUserAsync(call: ApplicationCall) { // the call: Application is a small package that holeds the request and respons
        val userId = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(
                HttpStatusCode.BadRequest,
                "User ID is not a number"
            )// read the id and cover it to int if possible if null badrequest
        val updateDto = call.receiveNullable<UserUpdateDto>()
            ?: return call.respond(
                HttpStatusCode.BadRequest,
                "Invalid or no update date"
            ) // receiveNullable checks if the UserUpdateDto is valid

        try {
            userRepository.updateUserAsync(userId, updateDto) // send the update to the UserRepository
            // if there is no user found trow NotFound message 404
            // null is not allowed for JSON respond
            val updatedUser = userRepository.getUserByIdAsync(userId)
                ?: return call.respond(HttpStatusCode.NotFound, "No user found after update")
            call.respond(
                HttpStatusCode.OK,
                updatedUser
            ) // send a HTTP Ok response back to the client with the updated user data
        } catch (e: SQLIntegrityConstraintViolationException) {
            // check if the error message is about UNIQUE or duplicate values in the database
            call.respond(HttpStatusCode.Conflict, "Username or email already exists")
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.NotFound, e.message ?: "User not found")
        } catch (e: SQLException) {
            // handle unexpected database errors
            call.application.environment.log.error("DB error while updating userprofile", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleDeleteUserAsync(call: ApplicationCall) {
        val userId = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "User ID is not a number")

        try {
            val isDeleted =
                userRepository.deleteUser(userId) // call the repository to start fun deleteUser and return boolean

            if (!isDeleted) {
                return call.respond(
                    HttpStatusCode.NotFound,
                    "User not found"
                ) // 404 code, return wil stop the action, so it will not continue
            }

            call.respond(HttpStatusCode.OK, "Account deleted successfully")

        } catch (e: SQLException) { // SQLException catch any SQL-related errors
            call.application.environment.log.error("DB error while deleting user with id $userId", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleFollowedGamesAsync(call: ApplicationCall) {
        val userId = call.parameters["id"]?.toIntOrNull()
            ?: return call.respond(HttpStatusCode.BadRequest, "User ID is not a number")
        val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

        try {
            val followedGames = followGameRepository.getFollowedGames(userId, offset, limit)

            call.respond(HttpStatusCode.OK, followedGames)
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while following games", e)
            call.respond(HttpStatusCode.InternalServerError)

        }
    }
}