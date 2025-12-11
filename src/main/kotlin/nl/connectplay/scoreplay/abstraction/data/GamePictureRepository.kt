package nl.connectplay.scoreplay.abstraction.data

import java.util.*

interface GamePictureRepository {
    suspend fun addGamePicture(gameId: Int, pictureId: UUID): Boolean
    suspend fun getGamePictureUrls(gameId: Int): List<String>
}