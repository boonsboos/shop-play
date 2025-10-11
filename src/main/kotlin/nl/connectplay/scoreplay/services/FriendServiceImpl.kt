package nl.connectplay.scoreplay.services

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import nl.connectplay.scoreplay.abstraction.data.FriendRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.abstraction.services.FriendService
import nl.connectplay.scoreplay.models.FriendshipStatus
import nl.connectplay.scoreplay.models.dto.UserDto
import nl.connectplay.scoreplay.models.dto.friend.UserFriendDto

class FriendServiceImpl(private val friendRepository: FriendRepository, private val userRepository: UserRepository) : FriendService {

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

        var status: FriendshipStatus = FriendshipStatus.PENDING

        // if the new friend has a pending friend request we should mark the users as now friends
        if (friendRepository.getFriendsAsync(newFriendId)?.contains(userId) ?: false) {
            // add an entry for the user that requested the friendship
            if(friendRepository.addFriendAsync(newFriendId, userId)) {
                status = FriendshipStatus.FRIENDS
            }
        }

        // we can now add the friend request for the user
        if(!friendRepository.addFriendAsync(userId, newFriendId)) {
            return null
        }

        // TODO: if the status is now pending,
        //  we should create a notification and route it to the new friend

        return status
    }

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
    }

    /**
     * Accepts a friend request from a user
     *
     * @param userId the id of the user accepting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    override suspend fun acceptFriendAsync(userId: Int, requesterUserId: Int) {
        friendRepository.addFriendAsync(userId, requesterUserId)
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

        val userList = mutableListOf<UserFriendDto>()
        for (friendId in friendIds) {
            val user = userRepository.getUserByIdAsync(friendId)
                ?: return null // we failed to fetch every user, stop executing

            // add users to list with friendship status
            userList.add(
                UserFriendDto(
                    user.username,
                    user.profilePicture,
                    FriendshipStatus.FRIENDS // we only have friends in this list
                )
            )
        }

        return userList.toList()
    }
}