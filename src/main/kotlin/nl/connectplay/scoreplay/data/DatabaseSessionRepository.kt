package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import java.util.UUID

/**
 * Repository for managing sessions that connects to our database.
 * Implements [SessionRepository]
 */
class DatabaseSessionRepository : SessionRepository {

    private val database = Database()

    val createSessionQuery: String = """
        INSERT INTO `sessions` (`game_id`, `host_user_id`, `session_visibility`)
        VALUES (?, ?, ?)
        RETURNING `session_id`
    """.trimIndent()

    /**
     * Creates a new session for a user
     * @param createDto the required data for making a new session
     * @return the ID of the newly created session
     * @throws java.sql.SQLException if data incorrect
     */
    override suspend fun createSession(createDto: CreateSessionDto): UUID? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val statement = connection.prepareStatement(createSessionQuery)

                    statement.setInt(1, createDto.gameId)
                    statement.setInt(2, createDto.userId)
                    statement.setInt(3, createDto.visibility.toInt())

                    val resultSet = statement.executeQuery()

                    val uuid = resultSet.getString("session_id")

                    UUID.fromString(uuid)
                }
            }.await()
        }
    }
}