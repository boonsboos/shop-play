package nl.connectplay.scoreplay.abstraction.services

interface FriendService {
    suspend fun isFriends(userId: Int, friendId: Int): Boolean?
    suspend fun isAlreadyRequested(userId: Int, friendId: Int?): Boolean?
    suspend fun requestFriend(userId: Int, newFriendId: Int)
    suspend fun removeFriend(userId: Int, friendId: Int)
    suspend fun rejectFriend(userId: Int, newFriendId: Int)
    suspend fun acceptFriend(userId: Int, newFriendId: Int)
}