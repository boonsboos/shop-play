package nl.connectplay.scoreplay.abstraction.services

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.utils.io.*
import nl.connectplay.scoreplay.models.dto.UploadPictureDto

interface PictureService {
    enum class EntityType {
        USER, Game, SESSION
    }

    suspend fun uploadImageByUrlAsync(url: String, entityType: EntityType, entityId: String): Boolean
    suspend fun handleUploadImageJsonAsync(
        uploadPicture: UploadPictureDto,
        entityType: EntityType,
        entityId: String,
    ): Pair<HttpStatusCode, Any>
}