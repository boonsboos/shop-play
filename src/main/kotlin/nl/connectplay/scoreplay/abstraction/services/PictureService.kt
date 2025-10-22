package nl.connectplay.scoreplay.abstraction.services

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.utils.io.*
import nl.connectplay.scoreplay.models.dto.picture.PictureDto
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import java.util.UUID

interface PictureService {
    enum class EntityType {
        USER, Game, SESSION
    }

    suspend fun uploadImageByUrlAsync(
        url: String,
        entityType: EntityType,
        entityId: String,
        userId: Int? = null
    ): Boolean

    suspend fun handleUploadImageJsonAsync(
        uploadPicture: UploadPictureDto,
        entityType: EntityType,
        entityId: String,
        userId: Int? = null
    ): Pair<HttpStatusCode, Any>

}