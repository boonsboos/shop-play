package nl.connectplay.scoreplay.data

import nl.connectplay.scoreplay.abstraction.data.ScoreRepository
import nl.connectplay.scoreplay.models.dto.ScoreDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.models.Score
import java.sql.SQLIntegrityConstraintViolationException
import java.util.UUID
import kotlin.use


class DatabaseScoreRepository : ScoreRepository {
    private val database = Database()

    override suspend fun getScoresAsync(
        limit: Int?, offset: Int?, userId: UUID?
    ): List<ScoreDto>? {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val scores = mutableListOf<ScoreDto>()

                    var sql = """
                        SELECT 
                            s.score_id,
                            s.session_id,
                            s.session_player_id,
                            s.game_iD,
                            s.score,
                            s.turn,
                            s.achieved_on
                        FROM scores AS s
                        JOIN games AS g ON s.game_id = g.game_id
                        JOIN sessions AS se ON s.session_id = se.session_id
                        LEFT JOIN session_players AS sp ON s.session_player_id = sp.session_player_id
                    """.trimIndent()

                    if (limit != null) {
                        sql += " LIMIT $limit"
                    }

                    if (offset != null) {
                        sql += " OFFSET=$offset"
                    }

                    println(sql)

                    val statement =
                        connection.prepareStatement(sql)
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

    override suspend fun addScoreAsync(score: ScoreDto): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val statement = connection.prepareStatement(
                        """
                        INSERT INTO scores (
                            score_id,
                            session_id,
                            session_player_id,
                            game_id,
                            score,
                            turn,
                            achieved_on
                            
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent()
                    )

                    statement.setString(1, score.scoreId.toString())
                    statement.setString(2, score.sessionId.toString())
                    statement.setString(3, score.sessionPlayerId.toString())
                    statement.setInt(4, score.gameId)
                    statement.setDouble(5, score.score)
                    statement.setInt(6, score.turn)
                    statement.setTimestamp(7, java.sql.Timestamp.valueOf(score.achievedOn))

                    val affectedRows = statement.executeUpdate()
                    statement.close()
                    return@async affectedRows > 0
                } catch (_: SQLIntegrityConstraintViolationException) {
                    return@async false // request already send
                }
            }
        }
    }.await() ?: false // we can return default false here because something went wrong


    override suspend fun updateScoreAsync(score: ScoreDto): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val statement = connection.prepareStatement(
                        """
                            UPDATE scores SET
                                score_id = ?,
                                session_id = ?,
                                session_player_id = ?,
                                game_id = ?,
                                score = ?,
                                turn = ?,
                                achieved_on = ?
                            WHERE score_id = ?
                        """.trimIndent()
                    )

                    statement.setString(1, score.scoreId.toString())
                    statement.setString(2, score.sessionId.toString())
                    statement.setString(3, score.sessionPlayerId.toString())
                    statement.setInt(4, score.gameId)
                    statement.setDouble(5, score.score)
                    statement.setInt(6, score.turn)
                    statement.setTimestamp(7, java.sql.Timestamp.valueOf(score.achievedOn))

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