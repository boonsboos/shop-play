package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.models.SessionPlayer
import nl.connectplay.scoreplay.models.SessionVisibility
import nl.connectplay.scoreplay.models.dto.game.GameDto
import nl.connectplay.scoreplay.models.dto.score.SessionPlayerDto
import nl.connectplay.scoreplay.models.dto.session.CreateSessionDto
import nl.connectplay.scoreplay.models.dto.session.SessionDto
import nl.connectplay.scoreplay.models.dto.session.UpdateSessionDto
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.util.*

/**
 * Repository for managing sessions that connects to our database.
 * Implements [SessionRepository]
 */
class DatabaseSessionRepository(private val database: Database) : SessionRepository {

    private val logger = LoggerFactory.getLogger(DatabaseSessionRepository::class.java)

    val createSessionQuery: String = """
        INSERT INTO `sessions` (`game_id`, `host_user_id`, `session_visibility`)
        VALUES (?, ?, ?)
        RETURNING `session_id`
    """.trimIndent()

    /**
     * Creates a new session for a user
     * @param createDto the required data for making a new session
     * @return the ID of the newly created session
     * @throws SQLException if data incorrect
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
                    if (resultSet.next()) {
                        val uuid = resultSet.getString("session_id")

                        return@async UUID.fromString(uuid)
                    }
                    return@async null
                }
            }.await()
        }
    }

    override suspend fun getSessionByIdAsync(sessionId: UUID): SessionDto? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                    SELECT s.session_id, s.host_user_id, s.start_time, s.end_time, s.session_visibility,
                        p.picture_url as end_of_session_picture,
                        g.game_id, g.scoring_method_id as game_scoring_method,
                        g.name as game_name,
                        g.description as game_description,
                        g.publisher as game_publisher,
                        g.minimum_player_count as game_min_players,
                        g.maximum_player_count as game_max_players,
                        g.duration as game_duration,
                        g.minimum_age as game_min_age,
                        g.release_date as game_release_date
                    FROM sessions as s
                    LEFT JOIN pictures AS p ON s.end_of_session_picture_id = p.picture_id
                    LEFT JOIN games AS g ON s.game_id = g.game_id
                    WHERE session_id = ?
                """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setObject(1, sessionId)

                val resultSet = stmt?.executeQuery()
                var session: SessionDto? = null;

                if (resultSet?.next() == true) {
                    println("Found session")
                    session = SessionDto(
                        sessionId = resultSet.getObject("session_id", UUID::class.java),
                        game = GameDto(
                            id = resultSet.getInt("game_id"),
                            scoringMethodId = resultSet.getInt("game_scoring_method"),
                            name = resultSet.getString("game_name"),
                            description = resultSet.getString("game_description"),
                            publisher = resultSet.getString("game_publisher"),
                            minPlayers = resultSet.getInt("game_min_players"),
                            maxPlayers = resultSet.getInt("game_max_players"),
                            duration = resultSet.getInt("game_duration"),
                            minAge = resultSet.getInt("game_min_age"),
                            releaseDate = resultSet.getDate("game_release_date")?.toLocalDate()
                                ?.toKotlinLocalDate(),
                        ),
                        hostId = resultSet.getInt("host_user_id"),
                        startTime = resultSet.getTimestamp("start_time").toLocalDateTime().toKotlinLocalDateTime(),
                        endTime = resultSet.getTimestamp("end_time")?.toLocalDateTime()?.toKotlinLocalDateTime(),
                        endOfSessionPictureUrl = resultSet.getString("end_of_session_picture"),
                        visibility = SessionVisibility.fromInt(resultSet.getInt("session_visibility")),
                    )
                }

                stmt?.close()
                resultSet?.close()

                session
            }
        }.await()
    }

    override suspend fun getSessionsAsync(userId: Int): List<SessionDto> = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                    SELECT s.session_id, s.host_user_id, s.start_time, s.end_time, s.session_visibility,
                        p.picture_url as end_of_session_picture,
                        g.game_id, g.scoring_method_id as game_scoring_method,
                        g.name as game_name,
                        g.description as game_description,
                        g.publisher as game_publisher,
                        g.minimum_player_count as game_min_players,
                        g.maximum_player_count as game_max_players,
                        g.duration as game_duration,
                        g.minimum_age as game_min_age,
                        g.release_date as game_release_date
                    FROM sessions as s
                    LEFT JOIN pictures AS p ON s.end_of_session_picture_id = p.picture_id
                    LEFT JOIN games AS g ON s.game_id = g.game_id   
                    WHERE host_user_id = ?
                """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setInt(1, userId)

                val sessions = mutableListOf<SessionDto>()

                val resultSet = stmt?.executeQuery()

                while (resultSet?.next() == true) {
                    sessions.add(
                        SessionDto(
                            sessionId = resultSet.getObject("session_id", UUID::class.java),
                            game = GameDto(
                                id = resultSet.getInt("game_id"),
                                scoringMethodId = resultSet.getInt("game_scoring_method"),
                                name = resultSet.getString("game_name"),
                                description = resultSet.getString("game_description"),
                                publisher = resultSet.getString("game_publisher"),
                                minPlayers = resultSet.getInt("game_min_players"),
                                maxPlayers = resultSet.getInt("game_max_players"),
                                duration = resultSet.getInt("game_duration"),
                                minAge = resultSet.getInt("game_min_age"),
                                releaseDate = resultSet.getDate("game_release_date")?.toLocalDate()
                                    ?.toKotlinLocalDate(),
                            ),
                            hostId = resultSet.getInt("host_user_id"),
                            startTime = resultSet.getTimestamp("start_time").toLocalDateTime().toKotlinLocalDateTime(),
                            endTime = resultSet.getTimestamp("end_time")?.toLocalDateTime()?.toKotlinLocalDateTime(),
                            endOfSessionPictureUrl = resultSet.getString("end_of_session_picture"),
                            visibility = SessionVisibility.fromInt(resultSet.getInt("session_visibility")),
                        )
                    )
                }

                stmt?.close()
                resultSet?.close()

                sessions.toList()
            }
        }.await() ?: emptyList()
    }

    override suspend fun updateSessionAsync(sessionId: UUID, userId: Int, updateSession: UpdateSessionDto): Boolean =
        coroutineScope {
            async {
                database.connection?.use { connection ->
                    try {
                        val sql = """
                            UPDATE sessions SET
                            end_time = COALESCE(?, end_time),
                            session_visibility = COALESCE(?, session_visibility)
                            WHERE session_id = ? AND host_user_id = ?
                        """.trimIndent()

                        val stmt = connection.prepareStatement(sql)

                        val computedEndTime = if (updateSession.endTime != null)
                            Timestamp.from(Instant.parse(updateSession.endTime))
                        else null

                        val computedVisibility = updateSession.visibility ?: SessionVisibility.PUBLIC.toInt()

                        stmt.setTimestamp(1, computedEndTime)
                        stmt.setInt(2, computedVisibility)
                        stmt.setObject(3, sessionId)
                        stmt.setInt(4, userId)

                        val affectedRow = stmt.executeUpdate()
                        stmt.close()
                        return@async affectedRow > 0
                    } catch (e: Exception) {
                        logger.error("Error updating session", e)
                        return@async false
                    }
                }
            }.await() ?: false
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

    override suspend fun deleteSessionAsync(userId: Int, sessionId: UUID): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val sql = """
                    DELETE FROM sessions WHERE session_id = ? and host_user_id = ?
                """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setObject(1, sessionId)
                    stmt.setInt(2, userId)


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

    override suspend fun getSessionPlayers(userId: Int): List<SessionPlayer> = coroutineScope {
        val sessionPlayers: MutableList<SessionPlayer> = mutableListOf()
        async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(
                    """
                    SELECT session_player_id, user_id, guest_name
                    FROM `session_players`
                    WHERE user_id = ?;
                    """.trimIndent()
                )

                statement.apply {
                    setInt(1, userId)
                }

                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    sessionPlayers.add(
                        SessionPlayer(
                            UUID.fromString(resultSet.getString("session_player_id")),
                            resultSet.getInt("user_id"),
                            resultSet.getString("guest_name")
                        )
                    )
                }
            }
        }.await()
        sessionPlayers.toList()
    }

    override suspend fun getSessionPlayerAsync(sessionPlayerId: UUID): SessionPlayer? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(
                    """
                    SELECT session_player_id, user_id, guest_name
                    FROM `session_players`
                    WHERE session_player_id = ?;
                    """.trimIndent()
                )

                statement.apply {
                    setString(1, sessionPlayerId.toString())
                }

                val resultSet = statement.executeQuery()

                var sessionPlayer: SessionPlayer? = null
                if (resultSet.next()) {
                    sessionPlayer = SessionPlayer(
                        UUID.fromString(resultSet.getString("session_player_id")),
                        resultSet.getInt("user_id"),
                        resultSet.getString("guest_name")
                    )
                }
                resultSet.close()
                statement.close()

                sessionPlayer
            }
        }.await()
    }

    override suspend fun createSessionPlayerAsync(sessionPlayer: SessionPlayerDto): SessionPlayer? =
        coroutineScope {
            val sessionPlayerId = async {
                database.connection?.use { connection ->
                    val statement = connection.prepareStatement(
                        """
                    INSERT INTO `session_players` (user_id, guest_name)
                    VALUES (?, ?)
                    RETURNING session_player_id;
                    """.trimIndent()
                    )

                    statement.apply {
                        setInt(1, sessionPlayer.userId)
                        setString(2, sessionPlayer.guest)
                    }

                    val resultSet = statement.executeQuery()

                    var sessionPlayer: UUID? = null
                    if (resultSet.next()) {
                        sessionPlayer = UUID.fromString(resultSet.getString("session_player_id"))
                    }

                    resultSet.close()
                    statement.close()
                    sessionPlayer
                }
            }

            getSessionPlayerAsync(sessionPlayerId.await()!!)
        }
}