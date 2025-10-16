package nl.connectplay.scoreplay.services

import io.ktor.http.*
import io.ktor.http.content.*
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.abstraction.data.PictureRepository
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.abstraction.services.CdnService
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.dto.UploadPictureDto
import java.util.*

class PictureServiceImpl(
    private val pictureRepository: PictureRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val gameRepository: GameRepository,
    private val cdnService: CdnService
) : PictureService {
    override suspend fun uploadImageAsync(
        file: MultiPartData, entityType: PictureService.EntityType, entityId: String
    ): Boolean {
        val url = cdnService.uploadImage(file)
        return false
    }

    override suspend fun handleUploadImageMultipartAsync(
        multipart: MultiPartData,
        entityType: PictureService.EntityType,
        entityId: String,
    ): Pair<HttpStatusCode, Any> {
        val success = uploadImageAsync(multipart, entityType, entityId)

        return if (success) {
            Pair(HttpStatusCode.Created, "Image uploaded")
        } else {
            Pair(HttpStatusCode.InternalServerError, "Failed to upload")
        }
    }

    override suspend fun uploadImageByUrlAsync(
        url: String, entityType: PictureService.EntityType, entityId: String
    ): Boolean {
        val pictureId = pictureRepository.addImageAsync(url) ?: return false

        when (entityType) {
            PictureService.EntityType.USER -> {
                userRepository.getUserByIdAsync(entityId.toInt()) ?: return false
                return userRepository.setProfilePictureAsync(entityId.toInt(), pictureId)
            }

            PictureService.EntityType.SESSION -> {
                sessionRepository.getSessionByIdAsync(UUID.fromString(entityId)) ?: return false
                return sessionRepository.setEndOfSessionPictureAsync(UUID.fromString(entityId), pictureId)
            }

            PictureService.EntityType.Game -> {
                gameRepository.getGameByIdAsync(entityId.toInt()) ?: return false
//                return gameRepository.addGameImagesAsync
                return false
            }
        }
    }

    override suspend fun handleUploadImageJsonAsync(
        uploadPicture: UploadPictureDto,
        entityType: PictureService.EntityType,
        entityId: String,
    ): Pair<HttpStatusCode, Any> {
        val success = uploadImageByUrlAsync(uploadPicture.pictureUrl, entityType, entityId)

        return if (success) {
            Pair(HttpStatusCode.Created, "Image uploaded")
        } else {
            Pair(HttpStatusCode.InternalServerError, "Failed to upload")
        }
    }

}