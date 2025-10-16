package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.abstraction.services.PictureService
import java.util.UUID

interface PictureRepository {
    suspend fun addImageAsync(url: String): UUID?
}