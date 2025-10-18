package nl.connectplay.scoreplay.data

import nl.connectplay.scoreplay.abstraction.data.ScoreRepository
import nl.connectplay.scoreplay.models.dto.score.ScoreDto
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.models.Score
import java.sql.SQLIntegrityConstraintViolationException
import java.util.UUID
import kotlin.use


class DatabaseScoreRepository : ScoreRepository {
    private val database = Database()

    override suspend fun getScoresAsync(
        limit: Int?, offset: Int?, sessionPlayerId: UUID?
    ): List<ScoreDto>? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val scores = mutableListOf<ScoreDto>()


                    var sql = """
                        SELECT 
                            score_id,
                            session_id,
                            session_player_id,
                            game_id,
                            score,
                            turn,
                            achieved_on
                        FROM scores
                            LIMIT ? OFFSET ?
                    """.trimIndent()

                    val stmt =
                        connection.prepareStatement(sql)
                        stmt.setInt(1, limit ?: 25)
                        stmt.setInt(2, offset ?: 0)
                    val resultSet = stmt.executeQuery()

                    while (resultSet?.next() == true) {
                        val score = ScoreDto(
                            scoreId = UUID.fromString(resultSet.getString("score_id")),
                            sessionId = UUID.fromString(resultSet.getString("session_id")),
                            sessionPlayerId = UUID.fromString(resultSet.getString("session_player_id")),
                            gameId = resultSet.getInt("game_id"),
                            score = resultSet.getDouble("score"),
                            turn = resultSet.getInt("turn"),
                            achievedOn = resultSet.getTimestamp("achieved_on").toLocalDateTime()
                        )
                        scores.add(score)
                    }

                    resultSet.close()
                    stmt.close()

                    return@async scores.toList()
                }
            }.await()
        }
    }

    override suspend fun getScoreByIdAsync(scoreId: UUID): Score? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val scores = mutableListOf<ScoreDto>()

                    val sql = """
                                SELECT 
                                score_id, 
                                session_id, 
                                session_player_id, 
                                game_id, 
                                score, 
                                turn, 
                                achieved_on
                                FROM scores
                                WHERE score_id = ?
                            """.trimIndent()

                    val statement = connection.prepareStatement(sql)
                    statement.setString(1, scoreId.toString())

                    val resultSet = statement.executeQuery()

                    while (resultSet?.next() == true) {
                        val score = ScoreDto(
                            scoreId = UUID.fromString(resultSet.getString("score_id")),
                            sessionId = UUID.fromString(resultSet.getString("session_id")),
                            sessionPlayerId = UUID.fromString(resultSet.getString("session_player_id")),
                            gameId = resultSet.getInt("game_id"),
                            score = resultSet.getDouble("score"),
                            turn = resultSet.getInt("turn"),
                            achievedOn = resultSet.getTimestamp("achieved_on").toLocalDateTime()
                        )
                        scores.add(score)
                    }

                    resultSet.close()
                    statement.close()

                    return@async scores.toList()
                }
            }.await() as Score?
        }
    }

    override suspend fun addScoreAsync(score: CreateScoreDto): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val statement = connection.prepareStatement(
                        """
                        INSERT INTO scores (
                            session_id,
                            session_player_id,
                            game_id,
                            score,
                            turn,
                            achieved_on
                            
                       ) VALUES (?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                    )

                    statement.setString(1, score.sessionId.toString())
                    statement.setString(2, score.sessionPlayerId.toString())
                    statement.setInt(3, score.gameId)
                    statement.setDouble(4, score.score)
                    statement.setInt(5, score.turn)
                    statement.setTimestamp(6, java.sql.Timestamp.valueOf(score.achievedOn))

                    val affectedRows = statement.executeUpdate()
                    statement.close()
                    return@async affectedRows > 0
                } catch (_: SQLIntegrityConstraintViolationException) {
                    return@async false // request already send
                }
            }
        }
    }.await() ?: false // we can return default false here because something went wrong


    override suspend fun updateScoreAsync(score: UpdateScoreDto): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val statement = connection.prepareStatement(
                        """
                            UPDATE scores SET
                                score = ?,
                                turn = ?
                            WHERE score_id = ?
                        """.trimIndent()
                    )

                    statement.setDouble(1, score.score)
                    statement.setInt(2, score.turn)


                    val affectedRows = statement.executeUpdate()
                    statement.close()
                    return@async affectedRows > 0
                } catch (_: SQLIntegrityConstraintViolationException) {
                    return@async false // request already send
                }
            }
        }.await() ?: false // we can return default false here because something went wrong
    }
}