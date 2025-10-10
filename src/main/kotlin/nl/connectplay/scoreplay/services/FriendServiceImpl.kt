package nl.connectplay.scoreplay.services

import nl.connectplay.scoreplay.abstraction.data.FriendRepository
import nl.connectplay.scoreplay.abstraction.services.FriendService

class FriendServiceImpl(private val friendRepository: FriendRepository) : FriendService {

    /**
     * Checks if users are already friends
     *
     * @param userId user id of the user to check friendship for
     * @param friendId user id of the possible friend
     */
    override suspend fun isFriends(userId: Int, friendId: Int): Boolean? {
        // we return null if something went wrong
        val userHasFriendEntry = friendRepository.getFriends(userId)?.contains(friendId) ?: return null
        val friendHasFriendEntry = friendRepository.getFriends(friendId)?.contains(userId) ?: return null

        // users are friends if there is a friend entry from both users
        return userHasFriendEntry && friendHasFriendEntry
    }

    /**
     * Requests a user to be the user's friend
     *
     * @param userId the id of the user making a new friend
     * @param newFriendId the id of the user being requested to be their friend
     */
    override suspend fun requestFriend(userId: Int, newFriendId: Int): Boolean {
        // if the user already has a pending friend request, we cannot let the user become friends
        if (friendRepository.getFriends(userId)?.contains(newFriendId) ?: false) {
            return false
        }
        return friendRepository.addFriend(userId, newFriendId)

    }

    /**
     * Removes a user as friend
     *
     * @param userId the id of the user removing the friend
     * @param friendId the id of the user no longer a friend
     */
    override suspend fun removeFriend(userId: Int, friendId: Int) {
        // remove both the friend entries to reset friend request status fully
        friendRepository.deleteFriend(userId, friendId)
        friendRepository.deleteFriend(friendId, userId)
    }

    /**
     * Rejects a friend request from a user
     *
     * @param userId the id of the user rejecting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    override suspend fun rejectFriend(userId: Int, requesterUserId: Int) {
        // the user who requests the friendship creates an entry, so we need to remove it in reverse
        friendRepository.deleteFriend(requesterUserId, userId)
    }

    /**
     * Accepts a friend request from a user
     *
     * @param userId the id of the user accepting the friendship
     * @param requesterUserId the id of the user who requested the friendship
     */
    override suspend fun acceptFriend(userId: Int, requesterUserId: Int) {
        friendRepository.addFriend(userId, requesterUserId)
    }
}