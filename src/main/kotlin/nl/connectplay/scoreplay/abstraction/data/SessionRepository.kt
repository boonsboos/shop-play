package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.Session
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import java.util.UUID

/**
 * Abstraction for defining data contracts in relation to [Session]s
 */
interface SessionRepository {

    /**
     * Creates a new session for a user
     * @param createDto the required data for making a new session
     * @return the ID of the newly created session
     */
    suspend fun createSessionAsync(createDto: CreateSessionDto): UUID?
    suspend fun getSessionByIdAsync(sessionId: UUID): SessionDto?
    suspend fun setEndOfSessionPictureAsync(sessionId: UUID, pictureId: UUID): Boolean
}