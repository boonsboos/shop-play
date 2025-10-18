package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.data.PictureRepository
import nl.connectplay.scoreplay.models.dto.picture.PictureDto
import java.util.*

class PictureController(private val pictureRepository: PictureRepository) {

    suspend fun handleGetPictureAsync(call: ApplicationCall) {
        val pictureId = UUID.fromString(call.parameters["id"] ?: return call.respond(HttpStatusCode.BadRequest))

        val url = pictureRepository.getPictureById(pictureId) ?: return call.respond(HttpStatusCode.NotFound)

        call.respond(HttpStatusCode.OK, PictureDto(url))
    }
}