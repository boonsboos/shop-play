package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import nl.connectplay.scoreplay.models.dto.UserDto

interface UserRepository {
    // because of 'suspend', the function will run asynchronously
    suspend fun getUsersAsync(limit: Int? = 25, offset: Int? = 0, query: String?): List<UserDto>?
    suspend fun getUserByIdAsync(userId: String): UserDto?
    suspend fun addUser(user: CreateUserDto)
}