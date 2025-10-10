package nl.connectplay.scoreplay.abstraction.data

interface FriendRepository {
    /**
     * Adds a friend entry for a user
     *
     * @param userId the id of the user to add a friend entry for
     * @param friendId the id of the user to be added as friend
     *
     * @return true on success, false on failure
     */
    suspend fun addFriend(userId: Int, friendId: Int): Boolean

    /**
     * Deletes a friend entry for the user
     *
     * @param userId the id of the user to delete a friend entry for
     * @param friendId the user id of the friend for which the entry will be deleted
     * @return true if success, false on failure
     */
    suspend fun deleteFriend(userId: Int, friendId: Int): Boolean

    /**
     * Gets the user ids of friends the user has
     *
     * @param userId the user to get the friend user ids for
     * @return list of user Ids
     */
    suspend fun getFriends(userId: Int): List<Int>?
}