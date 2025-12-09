package nl.connectplay.scoreplay.services

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import nl.connectplay.scoreplay.abstraction.data.FriendRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.abstraction.services.FriendService
import nl.connectplay.scoreplay.models.FriendshipStatus
import nl.connectplay.scoreplay.models.dto.friend.FriendRequestListResponse
import nl.connectplay.scoreplay.models.dto.friend.UserFriendDto
import nl.connectplay.scoreplay.models.events.FriendRequestEvent
import nl.connectplay.scoreplay.models.events.FriendRequestReplyEvent

class FriendServiceImpl(private val friendRepository: FriendRepository, private val userRepository: UserRepository, private val eventRouter: EventRoutingService) : FriendService {

    /**
     * Checks if users are already friends
     *
     * @param userId user id of the user to check friendship for
     * @param friendId user id of the possible friend
     */
    override suspend fun isFriendsAsync(userId: Int, friendId: Int): Boolean? {
        // we return null if something went wrong
        val userHasFriendEntry = friendRepository.getFriendsAsync(userId)?.contains(friendId) ?: return null
        val friendHasFriendEntry = friendRepository.getFriendsAsync(friendId)?.contains(userId) ?: return null

        // users are friends if there is a friend entry from both users
        return userHasFriendEntry && friendHasFriendEntry
    }

    /**
     * Requests a user to be the user's friend
     *
     * @param userId the id of the user making a new friend
     * @param newFriendId the id of the user being requested to be their friend
     * @return the status of the friendship or null if something fails
     * @throws [IllegalStateException] if user making a friend has a pending friend request to this user already
     */
    override suspend fun requestFriendAsync(userId: Int, newFriendId: Int): FriendshipStatus? {
        // if the user already has a pending friend request, we cannot let them send another request
        if (friendRepository.getFriendsAsync(userId)?.contains(newFriendId) ?: false) {
            // this state is not allowed, so we throw an illegal state exception
            throw IllegalStateException("Friend request is already pending!")
        }

        // if the new friend has a pending friend request we should mark the users as now friends
        if (friendRepository.getFriendsAsync(newFriendId)?.contains(userId) ?: false) {
            // add an entry for the user that requested the friendship
            if(friendRepository.addFriendAsync(newFriendId, userId)) {
                return FriendshipStatus.FRIENDS
            }
        }

        // we can now add the friend request for the user
        if(!friendRepository.addFriendAsync(userId, newFriendId)) {
            return null
        }

        sendFriendRequestEventAsync(newFriendId, userId)

        return FriendshipStatus.PENDING
    }

    private suspend fun sendFriendRequestEventAsync(friendRequestTargetId: Int, userId: Int) =
        eventRouter.routeEventAsync(friendRequestTargetId, FriendRequestEvent(userId))

    private suspend fun sendFriendRequestResponseEventAsync(friendRequestSenderId: Int, friendRequestReceiverId: Int, accepts: Boolean) =
        eventRouter.routeEventAsync(
            friendRequestSenderId,
            FriendRequestReplyEvent(friendRequestReceiverId, accepts)
        )

    /**
     * Removes a user as friend
     *
     * @param userId the id of the user removing the friend
     * @param friendId the id of the user no longer a friend
     */
    override suspend fun removeFriendAsync(userId: Int, friendId: Int) {
        // remove both the friend entries to reset friend request status fully
        friendRepository.deleteFriendAsync(userId, friendId)
        friendRepository.deleteFriendAsync(friendId, userId)
    }

    /**
     * Rejects a friend request from a user
     *
     * @param userId the id of the user rejecting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    override suspend fun rejectFriendAsync(userId: Int, requesterUserId: Int) {
        // the user who requests the friendship created an entry,
        // so we need to remove it with their ID as key
        friendRepository.deleteFriendAsync(requesterUserId, userId)
        sendFriendRequestResponseEventAsync(requesterUserId, userId, false)
    }

    /**
     * Accepts a friend request from a user
     *
     * @param userId the id of the user accepting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    override suspend fun acceptFriendAsync(userId: Int, requesterUserId: Int) {
        friendRepository.addFriendAsync(userId, requesterUserId)
        sendFriendRequestResponseEventAsync(requesterUserId, userId, true)
    }

    /**
     * Gets the friends for a user
     *
     * @param userId the user to get the friends for
     * @param limit standard limit parameter
     * @param offset standard offset parameter
     */
    override suspend fun getFriendsAsync(
        userId: Int,
        limit: Int,
        offset: Int
    ): List<UserFriendDto>?  {
        val friendIds = friendRepository.getFriendsAsync(userId, limit, offset) ?: return null

        return mapToUserFriendDto(friendIds)
    }

    /**
     * Gets the open friend requests for the user.
     *
     * @param userId the user to get friend requests for
     * @return [FriendRequestListResponse] containing pending (incoming) and outstanding (outgoing) friend requests
     */
    override suspend fun getFriendRequestsAsync(userId: Int): FriendRequestListResponse {
        var pendingIds = friendRepository.getPendingFriendRequestsAsync(userId) ?: listOf()
        var outstandingIds = friendRepository.getOutstandingFriendRequestsAsync(userId) ?: listOf()

        val friendIds = friendRepository.getFriendsAsync(userId) ?: listOf()

        pendingIds = pendingIds.filter { !friendIds.contains(it) }
        outstandingIds = outstandingIds.filter { !friendIds.contains(it) }

        return FriendRequestListResponse(
            mapToUserFriendDto(pendingIds, FriendshipStatus.PENDING),
            mapToUserFriendDto(outstandingIds, FriendshipStatus.PENDING)
        )
    }

    private suspend fun mapToUserFriendDto(friendIds: List<Int>, status: FriendshipStatus = FriendshipStatus.FRIENDS) =
        friendIds.asFlow().map { friendId ->
            coroutineScope {
                async {
                    val user = userRepository.getUserByIdAsync(friendId)
                        ?: throw IllegalArgumentException("Cannot fetch user for this friend") // we failed to fetch every user, stop executing

                    // add users to list with friendship status
                    UserFriendDto(
                        user.toUserDto(),
                        status
                    )
                }.await()
            }
        }.toList()
}