package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.ScoreRepository
import nl.connectplay.scoreplay.models.Score
import nl.connectplay.scoreplay.models.dto.score.CreateScoreDto
import nl.connectplay.scoreplay.models.dto.score.UpdateScoreDto
import org.slf4j.LoggerFactory
import java.util.*


class DatabaseScoreRepository(private val database: Database) : ScoreRepository {

    private val logger = LoggerFactory.getLogger(DatabaseScoreRepository::class.java)

    val getScoresSql = """
        SELECT 
            score_id,
            session_id,
            session_player_id,
            game_id,
            score,
            turn,
            achieved_on
        FROM scores
        WHERE session_id = ?
    """.trimIndent()

    override suspend fun getScoresAsync(
        sessionId: UUID
    ): List<Score> = coroutineScope {
        val scores = mutableListOf<Score>()
        async {
            database.connection?.use { connection ->

                val stmt = connection.prepareStatement(getScoresSql)
                stmt.setString(1, sessionId.toString())
                val resultSet = stmt.executeQuery()

                while (resultSet.next()) {
                    scores.add(
                        Score(
                            scoreId = UUID.fromString(resultSet.getString("score_id")),
                            sessionId = UUID.fromString(resultSet.getString("session_id")),
                            sessionPlayerId = UUID.fromString(resultSet.getString("session_player_id")),
                            gameId = resultSet.getInt("game_id"),
                            score = resultSet.getDouble("score"),
                            turn = resultSet.getInt("turn"),
                            achievedOn = resultSet.getTimestamp("achieved_on").toLocalDateTime()
                        )
                    )
                }

                resultSet.close()
                stmt.close()
            }
        }.await()

        scores.toList()
    }

    val getScoreByIdSql = """
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

    override suspend fun getScoreByIdAsync(scoreId: UUID): Score? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(getScoreByIdSql)
                statement.setString(1, scoreId.toString())

                val resultSet = statement.executeQuery()

                var score: Score? = null

                if (resultSet.next()) {
                    score = Score(
                        scoreId = UUID.fromString(resultSet.getString("score_id")),
                        sessionId = UUID.fromString(resultSet.getString("session_id")),
                        sessionPlayerId = UUID.fromString(resultSet.getString("session_player_id")),
                        gameId = resultSet.getInt("game_id"),
                        score = resultSet.getDouble("score"),
                        turn = resultSet.getInt("turn"),
                        achievedOn = resultSet.getTimestamp("achieved_on").toLocalDateTime()
                    )
                }

                resultSet.close()
                statement.close()

                score
            }
        }.await()
    }

    val addScoreSql = """    
       INSERT INTO scores (
            session_id,
            session_player_id,
            game_id,
            score,
            turn
       ) VALUES (?, ?, ?, ?, ?)
       RETURNING score_id;
    """.trimIndent()


    override suspend fun addScoreAsync(sessionID: UUID, sessionPlayerId: UUID, gameId: Int, score: CreateScoreDto): Score? = coroutineScope {
        val scoreId = async {
            database.connection?.use { connection ->
                val statement = connection.prepareStatement(addScoreSql)

                statement.setString(1, sessionID.toString())
                statement.setString(2, sessionPlayerId.toString())
                statement.setInt(3, gameId)
                statement.setDouble(4, score.score)
                statement.setInt(5, score.turn)

                val resultSet = statement.executeQuery()

                var result: UUID? = null
                if (resultSet.next()) {
                    result = UUID.fromString(resultSet.getString("score_id"))
                }

                statement.close()
                resultSet.close()

                result
            }
        }
        // we can assume the score id is not null
        // because the error will have occurred earlier in the coroutine
        getScoreByIdAsync(scoreId.await()!!)
    }

    val updateScoreSql = """
        UPDATE scores SET
            score = ?,
            turn = ?
        WHERE score_id = ?;
    """.trimIndent()

    override suspend fun updateScoreAsync(scoreId: UUID,score: UpdateScoreDto): Score? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val updateStatement = connection.prepareStatement(updateScoreSql)

                updateStatement.setDouble(1, score.score)
                updateStatement.setInt(2, score.turn)
                updateStatement.setString(3, scoreId.toString())

                updateStatement.executeUpdate()
                updateStatement.close()
            }
        }.await()

        getScoreByIdAsync(scoreId)
    }
}