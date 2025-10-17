package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.GameDto

interface GameRepository {
    suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>?
    suspend fun getFollowedGames(userId: Int, offset: Int, limit: Int? ): List<GameDto>
}