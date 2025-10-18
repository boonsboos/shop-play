package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.GameDto

interface GameRepository {
    suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>?
}