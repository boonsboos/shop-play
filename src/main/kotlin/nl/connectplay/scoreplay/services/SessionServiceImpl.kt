package nl.connectplay.scoreplay.services

import io.ktor.http.*
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.services.FriendService
import nl.connectplay.scoreplay.abstraction.services.SessionService
import nl.connectplay.scoreplay.models.SessionVisibility
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import org.slf4j.LoggerFactory
import java.util.*

class SessionServiceImpl(
    private val sessionRepository: SessionRepository,
    private val friendService: FriendService
) : SessionService {

    private val logger = LoggerFactory.getLogger(SessionServiceImpl::class.java)

    override suspend fun getSessionsAsync(
        userId: Int,
        targetId: Int,
        limit: Int,
        offset: Int
    ): Pair<HttpStatusCode, List<SessionDto>?> {
        try {
            val sessions: List<SessionDto> = sessionRepository.getSessionsAsync(targetId)
            if (sessions.isEmpty()) return HttpStatusCode.NoContent to null

            val friendStatus = friendService.isFriendsAsync(userId, targetId)
                ?: false

            // authorization filtering
            val filteredSessions = if (userId == targetId) {
                sessions // short circuit if the authed user is the same as the target user
            } else {
                sessions.filter {
                    it.visibility == SessionVisibility.PUBLIC
                            || it.visibility == SessionVisibility.ANONYMISED
                            || (friendStatus && it.visibility == SessionVisibility.FRIENDS_ONLY)
                            || userId == it.hostId
                }
            }

            if (filteredSessions.isEmpty()) return HttpStatusCode.NoContent to null

            // return the index of the last element in the filtered list if possible
            val computedOffset = if (offset > filteredSessions.size) {
                filteredSessions.size - 1
            } else {
                offset
            }

            // the limit should be 1 if we are were to go over the limit
            val computedLimit = if (computedOffset + limit > filteredSessions.size) {
                1
            } else {
                limit
            }

            val sessionsSlice = filteredSessions.subList(computedOffset, computedLimit)

            return Pair(HttpStatusCode.OK, sessionsSlice)
        } catch (e: Exception) {
            logger.error("Error while fetching sessions", e)
            return HttpStatusCode.InternalServerError to null
        }
    }

    override suspend fun getSessionAsync(
        userId: Int,
        targetId: Int,
        sessionId: UUID
    ): Pair<HttpStatusCode, SessionDto?> {
        try {
            val session: SessionDto = sessionRepository.getSessionByIdAsync(sessionId)
                ?: return HttpStatusCode.NotFound to null

            // if the target user is not the host of the session, we also return not found
            if (session.hostId != targetId) {
                return HttpStatusCode.NotFound to null
            }

            val friendStatus = friendService.isFriendsAsync(userId, targetId)
                ?: false

            return if (session.visibility == SessionVisibility.PUBLIC
                || session.visibility == SessionVisibility.ANONYMISED
                || (friendStatus && session.visibility == SessionVisibility.FRIENDS_ONLY)
                || userId == targetId
            )
                Pair(HttpStatusCode.OK, session)
            else
                HttpStatusCode.NotFound to null

        } catch (e: Exception) {
            logger.error("Error while fetching single session", e)
            return HttpStatusCode.InternalServerError to null
        }
    }
}