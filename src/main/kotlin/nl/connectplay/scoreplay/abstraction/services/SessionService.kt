package nl.connectplay.scoreplay.abstraction.services

import io.ktor.http.*
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import java.util.*

interface SessionService {
    /**
     * Get all user sessions
     * @param userId the ID user requesting the session
     * @param targetId the ID of the user that owns the session
     * @param limit the amount of sessions returned
     * @param offset the starting point to get all sessions
     * @return [Pair] of [HttpStatusCode] and [List] of [SessionDto]'s
     * where the list is not empty if the user may see the sessions,
     * or if it is found
     */
    suspend fun getSessionsAsync(
        userId: Int,
        targetId: Int,
        limit: Int,
        offset: Int
    ): Pair<HttpStatusCode, List<SessionDto>?>

    /**
     * Gets a single session from a user
     * @param userId the ID user requesting the session
     * @param targetId the ID of the user that owns the session
     * @param sessionId the ID of the requested session
     * @return [Pair] of [HttpStatusCode] and [SessionDto]
     * where the DTO is not null if the user may see the session,
     * or if it is found
     */
    suspend fun getSessionAsync(userId: Int, targetId: Int, sessionId: UUID): Pair<HttpStatusCode, SessionDto?>
}