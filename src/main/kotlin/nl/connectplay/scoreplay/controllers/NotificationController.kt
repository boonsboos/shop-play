package nl.connectplay.scoreplay.controllers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.sse.*
import io.ktor.utils.io.*
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.utilities.getLimitQueryParameter
import nl.connectplay.scoreplay.utilities.getOffsetQueryParameter
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.UUID

class NotificationController(private val notificationRepository: NotificationRepository, private val queueManagerService: EventQueueManagerService) {

    private val logger = LoggerFactory.getLogger(NotificationController::class.java)


    suspend fun handleSseSession(session: ServerSSESessionWithSerialization) {
        val userId = session.call.getUserIdFromJWT()

        logger.info("Starting SSE event session with user $userId, provisioning queue")
        val eventQueue = queueManagerService.provisionQueue(userId)

        // continuously try to send events
        try {
            for (event in eventQueue) {
                logger.info("Sending event ${event.javaClass.simpleName} to user $userId")
                session.send(event)
            }
        } catch (e: ClosedWriteChannelException) {
            logger.error("SSE connection with user $userId was closed, cleaning up")
        } catch (e: Exception) {
            logger.error("SSE connection with user $userId errored", e)
        }

        logger.info("Stopping SSE session with user $userId, removing queue")
        queueManagerService.removeQueue(userId)
    }

    suspend fun handleListByUserIdAsync(call: ApplicationCall) {
        val userIdParam = call.parameters["userId"]
            ?: return call.respond(HttpStatusCode.NotFound, "Missing user id")

        val userId = userIdParam.toInt()

        val limit = call.request.getLimitQueryParameter()
        val offset = call.request.getOffsetQueryParameter()

        val notifications = notificationRepository.getNotificationByUserAsync(userId, limit, offset)
            ?: return call.respond(HttpStatusCode.NotFound)

        call.respond(HttpStatusCode.OK, notifications)
    }

    suspend fun handleDeleteNotificationAsync(call: ApplicationCall) {
        val notificationIdParam = call.parameters["notificationId"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val notificationId = UUID.fromString(notificationIdParam)

        try {
            val isDeleted = notificationRepository.deleteNotificationAsync(notificationId)

            if (!isDeleted) {
                return call.respond(HttpStatusCode.NotFound)
            }

            call.respond(HttpStatusCode.OK)

        } catch (e: SQLException) {
            call.application.environment.log.error("DB error while deleting notification $notificationId", e)
            call.respond(HttpStatusCode.InternalServerError, "Failed to delete notification")
        }
    }

        suspend fun handleMarkAsReadAsync(call: ApplicationCall) {
            val notificationIdParam = call.parameters["notificationId"]
                ?: return call.respond(HttpStatusCode.BadRequest)

            val notificationId = UUID.fromString(notificationIdParam)

            try {
                val updated = notificationRepository.setNotificationAsReadAsync(notificationId)
                if (updated) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: SQLException) {
                call.application.environment.log.error("DB error while marking notification as read: $notificationId" , e)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }



}
