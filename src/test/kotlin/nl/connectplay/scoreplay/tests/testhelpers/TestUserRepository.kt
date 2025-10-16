package nl.connectplay.scoreplay.tests.testhelpers

import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.User
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import nl.connectplay.scoreplay.models.dto.user.UserDto
import nl.connectplay.scoreplay.models.dto.user.UserUpdateDto
import java.util.UUID

class TestUserRepository : UserRepository {
    val users = mutableMapOf<Int, UserDto>()

    override suspend fun getUsersAsync(
        limit: Int?,
        offset: Int?,
        query: String?
    ): List<UserDto> = users.values.toList()

    override suspend fun getUserByIdAsync(userId: Int): UserDto? = users[userId]
    override suspend fun getUserByNameOrEmailAsync(
        username: String?,
        email: String?
    ): User = User(1, username ?: "" , email ?: "", "", UUID.randomUUID())

    override suspend fun addUserAsync(user: CreateUserDto) {
        this.users[this.users.size] = UserDto(user.username, user.email, "")
    }

    override suspend fun updateUserAsync(
        userId: Int,
        updateDto: UserUpdateDto
    ) = run {  }

    override suspend fun deleteUserAsync(userId: Int): Boolean = true
}