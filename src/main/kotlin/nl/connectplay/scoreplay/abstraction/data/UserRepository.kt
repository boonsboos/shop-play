package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import nl.connectplay.scoreplay.models.dto.UserDto

interface UserRepository {
    // because of 'suspend', the function will run asynchronously
    suspend fun getUsersAsync(limit: Int?, offset: Int?, query: String?): List<UserDto>?
    suspend fun getUserByIdAsync(userId: Int): UserDto?
    suspend fun addUser(user: CreateUserDto)
}