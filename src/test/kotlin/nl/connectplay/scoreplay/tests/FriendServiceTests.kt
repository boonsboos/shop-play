package nl.connectplay.scoreplay.tests

import kotlinx.coroutines.runBlocking
import nl.connectplay.scoreplay.abstraction.data.FriendRepository
import nl.connectplay.scoreplay.services.FriendServiceImpl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.sql.SQLException
import kotlin.test.Test

class FriendServiceTests {

    class TestFriendRepository : FriendRepository {
        val friends = mutableMapOf<Int, Int>()

        override suspend fun addFriend(userId: Int, friendId: Int): Boolean {
            if (friends.containsKey(userId)) { return false }
            friends[userId] = friendId
            return true
        }

        override suspend fun deleteFriend(userId: Int, friendId: Int): Boolean {
            friends.remove(userId, friendId)
            return true
        }

        override suspend fun getFriends(userId: Int): List<Int> {
            return friends.filter{ it.key == userId }.values.toList()
        }
    }

    @Test
    fun testFriendsIntersect() {
        // Arrange
        val friendRepository: FriendRepository = TestFriendRepository()
        val friendService = FriendServiceImpl(friendRepository)

        runBlocking {
            friendRepository.addFriend(1, 2)
            friendRepository.addFriend(2, 1)
        }

        // Act
        val result = runBlocking { friendService.isFriends(1, 2) } ?: false

        // Assert
        assertTrue(result)
    }

    @Test
    fun testFriendsDoNotIntersect() {
        // Arrange
        val friendRepository: FriendRepository = TestFriendRepository()
        val friendService = FriendServiceImpl(friendRepository)

        runBlocking {
            friendRepository.addFriend(1, 2)
            friendRepository.addFriend(2, 3)
        }

        // Act
        val result = runBlocking { friendService.isFriends(1, 2) } ?: true

        // Assert
        assertFalse(result)
    }

    @Test
    fun testFriendsCannotBeDuplicate() {
        // Arrange
        val friendRepository: FriendRepository = TestFriendRepository()
        val friendService = FriendServiceImpl(friendRepository)

        runBlocking {
            friendRepository.addFriend(1, 2)
        }

        // Act
        val addingResult = runBlocking { friendService.requestFriend(1, 2) }

        // Assert
        assertFalse(addingResult)
    }
}