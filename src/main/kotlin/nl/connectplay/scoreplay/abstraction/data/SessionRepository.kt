package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.Session
import nl.connectplay.scoreplay.models.SessionPlayer
import nl.connectplay.scoreplay.models.dto.score.SessionPlayerDto
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import nl.connectplay.scoreplay.models.dto.session.UpdateSessionDto
import java.util.*

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
    suspend fun getSessionByIdAsync(sessionId: UUID, userId: Int): SessionDto?
    suspend fun getSessionsAsync(userId: Int): List<SessionDto>
    suspend fun updateSessionAsync(sessionId: UUID, userId: Int, updateSession: UpdateSessionDto): Boolean
    suspend fun setEndOfSessionPictureAsync(sessionId: UUID, pictureId: UUID): Boolean

    /**
     * Creates a new session player
     */
    suspend fun createSessionPlayerAsync(sessionPlayer: SessionPlayerDto): SessionPlayer?

    /**
     * Gets all session players for a user
     */
    suspend fun getSessionPlayers(userId: Int): List<SessionPlayer>

    /**
     * Gets a session player by ID
     */
    suspend fun getSessionPlayerAsync(sessionPlayerId: UUID): SessionPlayer?
}