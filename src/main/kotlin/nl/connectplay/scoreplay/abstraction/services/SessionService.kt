package nl.connectplay.scoreplay.abstraction.services

import io.ktor.http.*
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import java.util.*

interface SessionService {
    suspend fun getSessionsAsync(userId: Int, targetId: Int, limit: Int, offset: Int): Pair<HttpStatusCode, List<SessionDto>?>
    suspend fun getSessionAsync(userId: Int, targetId: Int, sessionId: UUID): Pair<HttpStatusCode, SessionDto?>
}