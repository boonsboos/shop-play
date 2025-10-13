package nl.connectplay.scoreplay.tests.testhelpers

import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.dto.CreateUserDto
import nl.connectplay.scoreplay.models.dto.UserDto

class TestUserRepository : UserRepository {
    val users = mutableMapOf<Int, UserDto>()

    override suspend fun getUsersAsync(
        limit: Int?,
        offset: Int?,
        query: String?
    ): List<UserDto> = users.values.toList()

    override suspend fun getUserByIdAsync(userId: Int): UserDto? = users[userId]

    override suspend fun addUser(user: CreateUserDto) {
        this.users[this.users.size] = UserDto(user.username, null)
    }
}