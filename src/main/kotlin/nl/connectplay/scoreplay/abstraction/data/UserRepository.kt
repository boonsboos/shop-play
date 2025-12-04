package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.user.CreateUserDto
import nl.connectplay.scoreplay.models.dto.user.FullUserDto
import nl.connectplay.scoreplay.models.dto.user.UserUpdateDto
import java.util.*

interface UserRepository {
    // because of 'suspend', the function will run asynchronously
    suspend fun getUsersAsync(limit: Int?, offset: Int?, query: String?): List<FullUserDto>?
    suspend fun getUserByIdAsync(userId: Int): FullUserDto?
    suspend fun getUserByNameOrEmail(username: String?, email: String?): User?
    suspend fun addUser(user: CreateUserDto)
    suspend fun updateUserAsync(userId: Int, updateDto: UserUpdateDto)
    suspend fun deleteUser(userId: Int): Boolean
    suspend fun setProfilePictureAsync(userId: Int, pictureId: UUID): Boolean
}