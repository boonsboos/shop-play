package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.models.SessionVisibility
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import java.sql.SQLException
import java.util.*

/**
 * Repository for managing sessions that connects to our database.
 * Implements [SessionRepository]
 */
class DatabaseSessionRepository(private val database: Database) : SessionRepository {

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
    override suspend fun createSessionAsync(createDto: CreateSessionDto): UUID? {
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

    override suspend fun getSessionByIdAsync(sessionId: UUID): SessionDto? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                    SELECT s.session_id, s.game_id, s.host_user_id, s.end_of_session_picture_id, s.session_visibility, p.picture_url
                    FROM sessions as s
                    LEFT JOIN pictures AS p ON u.profile_picture = p.picture_id
                    WHERE session_id = ?
                """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setObject(1, sessionId)

                val resultSet = stmt?.executeQuery()
                var session: SessionDto? = null;

                if (resultSet?.next() == true) {
                    session = SessionDto(
                        resultSet.getObject("session_id", UUID::class.java),
                        resultSet.getInt("game_id"),
                        resultSet.getInt("host_user_id"),
                        resultSet.getString("end_of_session_picture"),
                        SessionVisibility.entries[resultSet.getInt("session_visibility")],
                    )
                }

                stmt?.close()
                resultSet?.close()

                session
            }
        }.await()
    }

    override suspend fun setEndOfSessionPictureAsync(sessionId: UUID, pictureId: UUID): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val sql = """
                        UPDATE sessions SET end_of_session_picture_id  = ?
                        WHERE session_id  = ?
                    """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setObject(1, pictureId)
                    stmt.setObject(2, sessionId)

                    val affectedRow = stmt.executeUpdate()
                    stmt.close()
                    return@async affectedRow > 0
                } catch (e: SQLException) {
                    e.printStackTrace()
                    return@async false
                }
            }
        }.await() ?: false
    }
}