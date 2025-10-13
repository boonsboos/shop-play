package nl.connectplay.scoreplay.tests

import kotlinx.coroutines.runBlocking
import nl.connectplay.scoreplay.abstraction.data.FriendRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.models.FriendshipStatus
import nl.connectplay.scoreplay.services.FriendServiceImpl
import nl.connectplay.scoreplay.tests.testhelpers.TestFriendRepository
import nl.connectplay.scoreplay.tests.testhelpers.TestUserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test


class FriendServiceTests {

    @Test
    fun testFriendsIntersect() {
        // Arrange
        val friendRepository: FriendRepository = TestFriendRepository()
        val userRepository: UserRepository = TestUserRepository()
        val friendService = FriendServiceImpl(friendRepository, userRepository)

        runBlocking {
            friendRepository.addFriendAsync(1, 2)
            friendRepository.addFriendAsync(2, 1)
        }

        // Act
        val result = runBlocking { friendService.isFriendsAsync(1, 2) } ?: false

        // Assert
        assertTrue(result)
    }

    @Test
    fun testFriendsDoNotIntersect() {
        // Arrange
        val friendRepository: FriendRepository = TestFriendRepository()
        val userRepository: UserRepository = TestUserRepository()
        val friendService = FriendServiceImpl(friendRepository, userRepository)

        runBlocking {
            friendRepository.addFriendAsync(1, 2)
            friendRepository.addFriendAsync(2, 3)
        }

        // Act
        val result = runBlocking { friendService.isFriendsAsync(1, 2) } ?: true

        // Assert
        assertFalse(result)
    }

    @Test
    fun testFriendsRequestsCannotBeDuplicate() {
        // Arrange
        val friendRepository = TestFriendRepository()
        val userRepository = TestUserRepository()
        val friendService = FriendServiceImpl(friendRepository, userRepository)

        runBlocking {
            friendRepository.friends[1] = 2
        }

        // Act & Assert
        assertThrows<IllegalStateException> {
            runBlocking { friendService.requestFriendAsync(1, 2) }
        }
    }

    @Test
    fun `test friend requests are resolved when the other user has a pending request`() {
        // Arrange
        val friendRepository = TestFriendRepository()
        val userRepository = TestUserRepository()
        val friendService = FriendServiceImpl(friendRepository, userRepository)

        runBlocking {
            friendRepository.friends[2] = 1
        }

        // Act
        val result = runBlocking { friendService.requestFriendAsync(1, 2) }

        // Assert
        assertEquals(FriendshipStatus.FRIENDS, result)
    }
}