package nl.connectplay.scoreplay.services

import io.ktor.http.*
import nl.connectplay.scoreplay.abstraction.data.GamePictureRepository
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.abstraction.data.PictureRepository
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.abstraction.services.PictureService
import nl.connectplay.scoreplay.models.dto.picture.PictureDto
import nl.connectplay.scoreplay.models.dto.picture.UploadPictureDto
import java.util.*

class PictureServiceImpl(
    private val pictureRepository: PictureRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val gameRepository: GameRepository,
    private val gamePictureRepository: GamePictureRepository,
) : PictureService {

    override suspend fun uploadImageByUrlAsync(
        url: String, entityType: PictureService.EntityType, entityId: String, userId: Int?
    ): Boolean {
        val pictureId = pictureRepository.addImageAsync(url) ?: return false

        when (entityType) {
            PictureService.EntityType.USER -> {
                userRepository.getUserByIdAsync(entityId.toInt()) ?: return false
                return userRepository.setProfilePictureAsync(entityId.toInt(), pictureId)
            }

            PictureService.EntityType.SESSION -> {
                if (userId == null) return false
                sessionRepository.getSessionByIdAsync(UUID.fromString(entityId), userId) ?: return false
                return sessionRepository.setEndOfSessionPictureAsync(UUID.fromString(entityId), pictureId)
            }

            PictureService.EntityType.Game -> {
                gameRepository.getGameByIdAsync(entityId.toInt()) ?: return false
                return gamePictureRepository.addGamePicture(entityId.toInt(), pictureId)
            }
        }
    }

    override suspend fun handleUploadImageJsonAsync(
        uploadPicture: UploadPictureDto,
        entityType: PictureService.EntityType,
        entityId: String,
        userId: Int?
    ): Pair<HttpStatusCode, Any> {
        val success = uploadImageByUrlAsync(uploadPicture.pictureUrl, entityType, entityId, userId)

        return if (success) {
            Pair(HttpStatusCode.Created, "Image uploaded")
        } else {
            Pair(HttpStatusCode.InternalServerError, "Failed to upload")
        }
    }
}