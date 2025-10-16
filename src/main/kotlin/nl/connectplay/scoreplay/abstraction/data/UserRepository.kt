package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import nl.connectplay.scoreplay.models.dto.user.UserDto
import nl.connectplay.scoreplay.models.dto.user.UserUpdateDto

interface UserRepository {
    // because of 'suspend', the function will run asynchronously
    suspend fun getUsersAsync(limit: Int?, offset: Int?, query: String?): List<UserDto>?
    suspend fun getUserByIdAsync(userId: Int): UserDto?
    suspend fun getUserByNameOrEmail(username: String?, email: String?): User?
    suspend fun addUser(user: CreateUserDto)
    suspend fun updateUserAsync(userId: Int, updateDto: UserUpdateDto)
    suspend fun deleteUser(userId: Int): Boolean
}