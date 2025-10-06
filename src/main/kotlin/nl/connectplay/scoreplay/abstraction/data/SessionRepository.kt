package nl.connectplay.scoreplay.abstraction.data

import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import java.util.UUID

interface SessionRepository {

    suspend fun createSession(createDto: CreateSessionDto): UUID?
}