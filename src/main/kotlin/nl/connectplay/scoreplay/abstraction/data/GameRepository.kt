package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.Game
import nl.connectplay.scoreplay.models.dto.game.CreateGameDto
import nl.connectplay.scoreplay.models.dto.game.UpdateGameDto

interface GameRepository {
    suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<Game>
    suspend fun addGame(create: CreateGameDto): Game?
    suspend fun updateGame(id: Int, update: UpdateGameDto): Game?
    suspend fun getGameByIdAsync(gameId: Int): Game?
}
