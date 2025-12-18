package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.game.GameDto
import nl.connectplay.scoreplay.models.dto.user.UserDto

interface FollowGameRepository {
    suspend fun getFollowedGames(userId: Int, offset: Int, limit: Int?): List<GameDto>
    suspend fun followGame(userId: Int, gameId: Int)
    suspend fun unfollowGame(userId: Int, gameId: Int)
    suspend fun getFollowers(gameId: Int, offset: Int, limit: Int?): List<UserDto>
    suspend fun getAllFollowerUserIdsAsync(gameId: Int): List<Int>
    suspend fun isFollowing(userId: Int, gameId: Int): Boolean
}
