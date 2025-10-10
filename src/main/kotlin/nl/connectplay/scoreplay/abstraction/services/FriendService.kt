package nl.connectplay.scoreplay.abstraction.services

interface FriendService {
    suspend fun isFriends(userId: Int, friendId: Int): Boolean?
    suspend fun requestFriend(userId: Int, newFriendId: Int): Boolean?
    suspend fun removeFriend(userId: Int, friendId: Int)
    suspend fun rejectFriend(userId: Int, requesterUserId: Int)
    suspend fun acceptFriend(userId: Int, requesterUserId: Int)
}