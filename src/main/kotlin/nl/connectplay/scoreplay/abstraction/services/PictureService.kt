package nl.connectplay.scoreplay.abstraction.services

import io.ktor.http.*
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto

interface PictureService {
    /**
     *
     */
    enum class EntityType {
        USER,
        GAME,
        SESSION
    }

    /**
     * Upload image by URL based on [entityType] and [entityId]
     */
    suspend fun uploadImageByUrlAsync(
        url: String,
        entityType: EntityType,
        entityId: String,
        userId: Int? = null
    ): Boolean

    /**
     * Handle the image by URL upload request based on [entityType] and [entityId]
     */
    suspend fun handleUploadImageJsonAsync(
        uploadPicture: UploadPictureDto,
        entityType: EntityType,
        entityId: String,
        userId: Int? = null
    ): Pair<HttpStatusCode, Any>

}