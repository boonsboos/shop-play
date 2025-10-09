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
        val userHasFriendEntry = friendRepository.getFriends(userId)?.contains(friendId)?: return null
        val friendHasFriendEntry = friendRepository.getFriends(friendId)?.contains(userId) ?: return null

        // users are friends if there is a friend entry from both users
        return userHasFriendEntry == friendHasFriendEntry
    }

    /**
     * Requests a user to be the user's friend
     *
     * @param userId the id of the user making a new friend
     * @param newFriendId the id of the user being requested to be their friend
     */
    override suspend fun requestFriend(userId: Int, newFriendId: Int) {
        friendRepository.addFriend(userId, newFriendId)
    }

    /**
     * Removes a user as friend
     *
     * @param userId the id of the user removing the friend
     * @param friendId the id of the user no longer a friend
     */
    override suspend fun removeFriend(userId: Int, friendId: Int) {
        friendRepository.deleteFriend(userId, friendId)
    }

    /**
     * Rejects a friend request from a user
     *
     * @param userId the id of the user rejecting the friendship
     * @param newFriendId the id of the user who requested the friendship
     */
    override suspend fun rejectFriend(userId: Int, newFriendId: Int) {
        friendRepository.deleteFriend(userId, newFriendId)
    }

    /**
     * Accepts a friend request from a user
     *
     * @param userId the id of the user accepting the friendship
     * @param newFriendId the id of the user who requested the friendship
     */
    override suspend fun acceptFriend(userId: Int, newFriendId: Int) {
        friendRepository.addFriend(userId, newFriendId)
    }
}