package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.GameDto
import nl.connectplay.scoreplay.models.dto.CreateGameDto

interface GameRepository {
    suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>?
    suspend fun addGame(create: CreateGameDto): GameDto
    suspend fun updateGame(id: Int, update: UpdateGameDto): GameDto?

}
