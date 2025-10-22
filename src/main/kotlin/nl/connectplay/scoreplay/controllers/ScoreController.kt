package nl.connectplay.scoreplay.controllers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import nl.connectplay.scoreplay.abstraction.services.ScoreService
import nl.connectplay.scoreplay.exceptions.NotFoundException
import nl.connectplay.scoreplay.exceptions.UnfinishedSessionException
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import nl.connectplay.scoreplay.utilities.getUUIDOrNull
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import org.slf4j.LoggerFactory
import java.sql.SQLException

class ScoreController(private val scoreService: ScoreService) {

    private val logger = LoggerFactory.getLogger(ScoreController::class.java)

    suspend fun handleListAsync(call: ApplicationCall) {
        // we don't take offset and limit here
        // because you always want all the scores in your session
        val userId = call.getUserIdFromJWT()
        val sessionId = call.getUUIDOrNull("sessionId")
            ?: return call.respond(HttpStatusCode.BadRequest, "Bad session ID")

        try {
            val scores = scoreService.getScoresAsync(sessionId, userId)

            call.respond(HttpStatusCode.OK, scores)
        } catch (e: SQLException) {
            logger.error("DB error while creating a score", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleOneAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = call.getUUIDOrNull("sessionId")
            ?: return call.respond(HttpStatusCode.BadRequest, "Bad session ID")
        val scoreId = call.getUUIDOrNull("scoreId")
            ?: return call.respond(HttpStatusCode.BadRequest, "Bad score ID")

        try {
            val score = scoreService.getScoreAsync(sessionId, userId, scoreId)

            call.respond(HttpStatusCode.OK, score)
        } catch (e: NotFoundException) {
            logger.error("Something was not found while fetching a single score", e)
            call.respond(HttpStatusCode.NotFound)
        } catch (e: IllegalStateException) {
            logger.error("Encountered illegal state while fetching single score", e)
            call.respond(HttpStatusCode.InternalServerError)
        } catch (e: SQLException) {
            logger.error("DB error while fetching single score", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleCreateAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = call.getUUIDOrNull("id")
            ?: return call.respond(HttpStatusCode.BadRequest, "Bad session id")

        val newScores = call.receiveNullable<List<CreateScoreDto>>()
            ?: return call.respond(HttpStatusCode.BadRequest)

        try {
            val score = scoreService.bulkUploadScoresAsync(sessionId, userId, newScores)

            call.respond(HttpStatusCode.Created, score)
        } catch (e: UnfinishedSessionException) {
            logger.error(e.message)
            call.respond(HttpStatusCode.Forbidden, "Session not yet finished")
        } catch (e: NotFoundException) {
            logger.error("Something was not found while uploading a score", e)
            call.respond(HttpStatusCode.NotFound)
        } catch (e: IllegalStateException) {
            logger.error("Encountered illegal state while creating score", e)
            call.respond(HttpStatusCode.InternalServerError)
        } catch (e: SQLException) {
            logger.error("DB error while creating a score", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    suspend fun handleUpdateAsync(call: ApplicationCall) {
        val userId = call.getUserIdFromJWT()
        val sessionId = call.getUUIDOrNull("id")
            ?: return call.respond(HttpStatusCode.BadRequest, "Bad session ID")
        val scoreId = call.getUUIDOrNull("scoreId")
            ?: return call.respond(HttpStatusCode.BadRequest, "Bad score ID")

        val updatedScore = call.receiveNullable<UpdateScoreDto>()
            ?: return call.respond(HttpStatusCode.BadRequest)

        try {
            val score = scoreService.updateScoreAsync(sessionId, userId, scoreId, updatedScore)

            call.respond(HttpStatusCode.OK, score)
        } catch (e: NotFoundException) {
            logger.error("Something was not found while updating score", e)
            call.respond(HttpStatusCode.NotFound)
        } catch (e: IllegalStateException) {
            logger.error("Encountered illegal state while updating score", e)
            call.respond(HttpStatusCode.InternalServerError)
        } catch (e: SQLException) {
            logger.error("DB error while updating a score", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

}