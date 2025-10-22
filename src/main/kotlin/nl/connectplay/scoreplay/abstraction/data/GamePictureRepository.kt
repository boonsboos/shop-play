package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.Picture
import java.util.UUID

interface GamePictureRepository {
    suspend fun addGamePicture(gameId: Int, pictureId: UUID): Boolean
}