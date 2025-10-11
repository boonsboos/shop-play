package nl.connectplay.scoreplay.tests.testhelpers

import nl.connectplay.scoreplay.abstraction.data.FriendRepository

class TestFriendRepository : FriendRepository {
    val friends = mutableMapOf<Int, Int>()

    override suspend fun addFriendAsync(userId: Int, friendId: Int): Boolean {
        friends[userId] = friendId
        return true
    }

    override suspend fun deleteFriendAsync(userId: Int, friendId: Int): Boolean {
        friends.remove(userId, friendId)
        return true
    }

    override suspend fun getFriendsAsync(userId: Int): List<Int> {
        return friends.values.toList()
    }

    override suspend fun getFriendsAsync(
        userId: Int,
        limit: Int,
        offset: Int
    ): List<Int> = getFriendsAsync(userId)
}