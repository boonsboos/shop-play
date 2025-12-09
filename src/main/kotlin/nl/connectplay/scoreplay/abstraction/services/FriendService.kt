package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.FriendshipStatus
import nl.connectplay.scoreplay.models.dto.friend.FriendRequestListResponse
import nl.connectplay.scoreplay.models.dto.friend.UserFriendDto

interface FriendService {
    /**
     * Checks if users are already friends
     *
     * @param userId user id of the user to check friendship for
     * @param friendId user id of the possible friend
     */
    suspend fun isFriendsAsync(userId: Int, friendId: Int): Boolean?

    /**
     * Requests a user to be the user's friend
     *
     * @param userId the id of the user making a new friend
     * @param newFriendId the id of the user being requested to be their friend
     * @return the status of the friendship or null if something fails
     * @throws [IllegalStateException] if user making a friend has a pending friend request to this user already
     */
    suspend fun requestFriendAsync(userId: Int, newFriendId: Int): FriendshipStatus?

    /**
     * Removes a user as friend
     *
     * @param userId the id of the user removing the friend
     * @param friendId the id of the user no longer a friend
     */
    suspend fun removeFriendAsync(userId: Int, friendId: Int)

    /**
     * Rejects a friend request from a user
     *
     * @param userId the id of the user rejecting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    suspend fun rejectFriendAsync(userId: Int, requesterUserId: Int)

    /**
     * Accepts a friend request from a user
     *
     * @param userId the id of the user accepting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    suspend fun acceptFriendAsync(userId: Int, requesterUserId: Int)

    /**
     * Gets the friends for a user
     *
     * @param userId the user to get the friends for
     * @param limit standard limit parameter
     * @param offset standard offset parameter
     */
    suspend fun getFriendsAsync(userId: Int, limit: Int, offset: Int): List<UserFriendDto>?

    /**
     * Gets the open friend requests for the user.
     *
     * @param userId the user to get friend requests for
     * @return [FriendRequestListResponse] containing pending (incoming) and outstanding (outgoing) friend requests
     */
    suspend fun getFriendRequestsAsync(userId: Int): FriendRequestListResponse
}