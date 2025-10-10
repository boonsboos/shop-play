package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.FriendshipStatus
import nl.connectplay.scoreplay.models.dto.UserDto

interface FriendService {
    suspend fun isFriendsAsync(userId: Int, friendId: Int): Boolean?
    suspend fun requestFriendAsync(userId: Int, newFriendId: Int): FriendshipStatus?
    suspend fun removeFriendAsync(userId: Int, friendId: Int)
    suspend fun rejectFriendAsync(userId: Int, requesterUserId: Int)
    suspend fun acceptFriendAsync(userId: Int, requesterUserId: Int)
    suspend fun getFriendsAsync(userId: Int, limit: Int, offset: Int): List<UserDto>?
}