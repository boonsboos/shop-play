package nl.connectplay.scoreplay.controllers

import io.ktor.server.sse.*
import io.ktor.utils.io.*
import nl.connectplay.scoreplay.abstraction.data.NotificationRepository
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.utilities.getUserIdFromJWT
import org.slf4j.LoggerFactory

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
}