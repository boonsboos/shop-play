package nl.connectplay.scoreplay.tests.testhelpers

import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.user.CreateUserDto
import nl.connectplay.scoreplay.models.dto.user.FullUserDto
import nl.connectplay.scoreplay.models.dto.user.UserUpdateDto
import java.util.*

class TestUserRepository : UserRepository {
    val users = mutableMapOf<Int, FullUserDto>()

    override suspend fun getUsersAsync(
        limit: Int?,
        offset: Int?,
        query: String?
    ): List<FullUserDto> = users.values.toList()

    override suspend fun getUserByIdAsync(userId: Int): FullUserDto? = users[userId]
    override suspend fun getUserByNameOrEmail(
        username: String?,
        email: String?
    ): User = User(1, username ?: "", email ?: "", "", UUID.randomUUID())

    override suspend fun addUser(user: CreateUserDto) {
        this.users[this.users.size] = FullUserDto(1, user.username, "", null)
    }

    override suspend fun updateUserAsync(
        userId: Int,
        updateDto: UserUpdateDto
    ) {
        return
    }

    override suspend fun deleteUser(userId: Int): Boolean = true

    override suspend fun setProfilePictureAsync(userId: Int, pictureId: UUID): Boolean = true
}