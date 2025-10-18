package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import nl.connectplay.scoreplay.abstraction.data.ScoreRepository
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import nl.connectplay.scoreplay.utilities.getLimitQueryParameter
import nl.connectplay.scoreplay.utilities.getOffsetQueryParameter
import java.sql.SQLException
import java.util.UUID

class ScoreController(
    private val scoreRepository: ScoreRepository) {

    // Get /Scores
    suspend fun handleListAsync(call: ApplicationCall) {
        val limit = call.request.getLimitQueryParameter()
        val offset = call.request.getOffsetQueryParameter()

        val scores = scoreRepository.getScoresAsync(limit, offset)
            ?: return call.respond(HttpStatusCode.NotFound)

            call.respond(HttpStatusCode.OK,scores)
    }

    //GET /Scored/ID
    suspend fun handleOneAsync(call: ApplicationCall) {
        val scoreIdParam = call.parameters["scoreId"] ?:
        return call.respond(HttpStatusCode.NotFound, "Missing score id")

        val scoreId = UUID.fromString(scoreIdParam)

        val score = scoreRepository.getScoreByIdAsync(scoreId)
            ?: return call.respond(HttpStatusCode.NotFound)

            call.respond(HttpStatusCode.OK, score)
    }

    // POST /Scores
    suspend fun handleCreateAsync(call: ApplicationCall) {
        val scoreDto = call.receiveNullable<CreateScoreDto>()
            ?: return call.respond(HttpStatusCode.BadRequest, "Missing score id")

        try {
            val created = scoreRepository.addScoreAsync(scoreDto)

            if (created) {
                call.respond(HttpStatusCode.Created, CreateScoreDto)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }catch (e: Exception) {
            call.application.environment.log.error("Exception", e)
            return call.respond(HttpStatusCode.InternalServerError)
        }
    }

    // PATCH /scores/ID
    suspend fun handleUpdateAsync(call: ApplicationCall) {
        val scoreIdParam =
            call.parameters["scoreId"] ?: return call.respond(HttpStatusCode.BadRequest, "Missing score id")

        val scoreId = UUID.fromString(scoreIdParam)

        val updatedScore = call.receiveNullable<UpdateScoreDto>()
            ?: return call.respond(HttpStatusCode.BadRequest, "Missing score id")


        if (updatedScore.scoreId != scoreId) {
            return call.respond(HttpStatusCode.BadRequest, "Score ID in path and body do not match")
        }

        try {
            val updated = scoreRepository.updateScoreAsync(updatedScore)
            if (updated) {
                call.respond(HttpStatusCode.OK,UpdateScoreDto)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while updating score $scoreId", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

}