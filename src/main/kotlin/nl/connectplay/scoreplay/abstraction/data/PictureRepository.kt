package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.dto.picture.PictureDto
import java.util.UUID

interface PictureRepository {
    suspend fun addImageAsync(url: String): UUID?
    suspend fun getPictureById(pictureId: UUID): String?

}