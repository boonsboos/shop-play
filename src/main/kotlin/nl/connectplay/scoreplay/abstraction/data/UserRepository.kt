package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.UserDto

interface UserRepository {
    suspend fun getUsersAsync(limit: Int? = 25, offset: Int? = 0, query: String?): List<UserDto>?
    suspend fun getUserByIdAsync(userId: String): UserDto?
}