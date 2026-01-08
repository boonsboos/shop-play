package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.Game
import nl.connectplay.scoreplay.models.dto.game.CreateGameDto
import nl.connectplay.scoreplay.models.dto.game.RecentGameDto
import nl.connectplay.scoreplay.models.dto.game.UpdateGameDto
import java.sql.Date
import java.sql.Statement

class DatabaseGameRepository(private val database: Database) : GameRepository {

    override suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<Game> = coroutineScope {
        async {
            // SQL Statement to db
            database.connection?.use { conn ->
                val sql = """
                    SELECT 
                        game_id, name, description, publisher, scoring_method_id,
                        minimum_player_count, maximum_player_count, duration, minimum_age, release_date
                    FROM games
                    WHERE (? IS NULL OR name LIKE ? OR publisher LIKE ? OR description LIKE ?)
                    ORDER BY game_id
                    LIMIT ? OFFSET ?
                """.trimIndent()

                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, query)
                    val like = "%${query ?: ""}%"
                    stmt.setString(2, like) // game
                    stmt.setString(3, like) // publisher
                    stmt.setString(4, like) // description
                    stmt.setInt(5, limit ?: 25)
                    stmt.setInt(6, offset ?: 0)

                    // Creates mutable list of Games using GameDto
                    stmt.executeQuery().use { rs ->
                        val list = mutableListOf<Game>()
                        while (rs.next()) {
                            list.add(
                                Game(
                                    id = rs.getInt("game_id"),
                                    name = rs.getString("name"),
                                    scoringMethodId = rs.getInt("scoring_method_id"),
                                    description = rs.getString("description"),
                                    publisher = rs.getString("publisher"),
                                    // getInt() returns 0 if value is SQL NULL, so we need to make sure it's mapped back to null
                                    minPlayers = rs.getInt("minimum_player_count").let { if (it > 0) it else null },
                                    maxPlayers = rs.getInt("maximum_player_count").let { if (it > 0) it else null },
                                    duration = rs.getInt("duration").let { if (it > 0) it else null },
                                    minAge = rs.getInt("minimum_age").let { if (it > 0) it else null },
                                    releaseDate = rs.getDate("release_date")?.toLocalDate()?.toKotlinLocalDate(),
                                )
                            )
                        }
                        list.toList() // Can be empty
                    }
                }
            }
        }.await() ?: listOf()
    }

    override suspend fun addGame(create: CreateGameDto): Game? = coroutineScope {
        async {
            // SQL Statement to db
            database.connection?.use { conn ->
                val sql = """
                    INSERT INTO games
                      (name, description, publisher, minimum_player_count, maximum_player_count, duration, minimum_age, release_date, scoring_method_id)
                    VALUES (?, ?,  ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setString(1, create.name)
                    stmt.setString(2, create.description)
                    stmt.setString(3, create.publisher)
                    stmt.setObject(4, create.minPlayers)
                    stmt.setObject(5, create.maxPlayers)
                    stmt.setObject(6, create.duration)
                    stmt.setObject(7, create.minAge)

                    // Convert kotlinx.datetime.LocalDate -> java.time.LocalDate -> java.sql.Date
                    val date = create.releaseDate?.let {
                        Date.valueOf(it.toJavaLocalDate())
                    }

                    stmt.setDate(8, date)
                    stmt.setInt(9, create.scoringMethod) // TODO: propagate from model
                    stmt.executeUpdate()

                    // Read generated id
                    val rs = stmt.generatedKeys
                    val generatedId =
                        if (rs.next()) rs.getInt(1) else throw IllegalStateException("Failed to retrieve generated id")
                    rs.close()

                    // Return created GameDto (without re-query; use provided fields + id)
                    Game(
                        id = generatedId,
                        scoringMethodId = create.scoringMethod,
                        name = create.name,
                        description = create.description,
                        publisher = create.publisher,
                        minPlayers = create.minPlayers,
                        maxPlayers = create.maxPlayers,
                        duration = create.duration,
                        minAge = create.minAge,
                        releaseDate = create.releaseDate
                    )
                }
            } ?: throw IllegalStateException("No database connection")
        }.await()
    }

    override suspend fun updateGame(id: Int, update: UpdateGameDto): Game? = coroutineScope {
        async {
            database.connection?.use { conn ->
                val sql = """
                    UPDATE games
                    SET
                        name = COALESCE(?, name),
                        description = COALESCE(?, description),
                        publisher = COALESCE(?, publisher),
                        minimum_player_count = COALESCE(?, minimum_player_count),
                        maximum_player_count = COALESCE(?, maximum_player_count),
                        duration = COALESCE(?, duration),
                        minimum_age = COALESCE(?, minimum_age),
                        release_date = COALESCE(?, release_date),
                        scoring_method_id = COALESCE(?, scoring_method_id)
                    WHERE game_id = ?
                """.trimIndent()

                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, update.name)
                    stmt.setString(2, update.description)
                    stmt.setString(3, update.publisher)
                    stmt.setObject(4, update.minPlayers)
                    stmt.setObject(5, update.maxPlayers)
                    stmt.setObject(6, update.duration)
                    stmt.setObject(7, update.minAge)
                    stmt.setObject(8, update.releaseDate?.let {
                        Date.valueOf(it.toJavaLocalDate())
                    })
                    stmt.setObject(9, update.scoringMethod)
                    stmt.setInt(10, id)

                    val updated = stmt.executeUpdate()
                    if (updated == 0) return@use null
                }
            }
        }.await()
        getGameByIdAsync(id)
    }


    override suspend fun getGameByIdAsync(gameId: Int): Game? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                    SELECT `game_id`, `name`, `scoring_method_id`, `description`, `publisher`, `minimum_player_count`, `maximum_player_count`, `duration`, `minimum_age`, `release_date`
                    FROM `games`
                    WHERE `game_id` = ?
                """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setInt(1, gameId)

                val resultSet = stmt?.executeQuery()
                var game: Game? = null;
                if (resultSet?.next() == true) {
                    game = Game(
                        id = resultSet.getInt("game_id"),
                        name = resultSet.getString("name"),
                        scoringMethodId = resultSet.getInt("scoring_method_id"),
                        description = resultSet.getString("description"),
                        publisher = resultSet.getString("publisher"),
                        minPlayers = resultSet.getInt("minimum_player_count").let { if (it > 0) it else null },
                        maxPlayers = resultSet.getInt("maximum_player_count").let { if (it > 0) it else null },
                        duration = resultSet.getInt("duration").let { if (it > 0) it else null },
                        minAge = resultSet.getInt("minimum_age").let { if (it > 0) it else null },
                        releaseDate = resultSet.getDate("release_date")?.toLocalDate()?.toKotlinLocalDate(),
                    )
                }

                stmt?.close()
                resultSet?.close()

                game
            }
        }.await()
    }

    private val getRecentGamesForUserSql = """
        SELECT `games`.`game_id`, `name`, `scoring_method_id`, `description`, `publisher`, `minimum_player_count`, `maximum_player_count`, `duration`, `minimum_age`, `release_date`, `achieved_on`
        FROM `games`
        JOIN `scores` ON `games`.`game_id` = `scores`.`game_id`
        JOIN `session_players` ON `session_players`.`session_player_id` = `scores`.`session_player_id`
        WHERE `session_players`.`user_id` = ?
        GROUP BY `games`.`game_id`
        HAVING MAX(`achieved_on`)
        ORDER BY `scores`.`achieved_on` DESC
        LIMIT 5;
    """.trimIndent()

    override suspend fun getRecentGamesForUser(userId: Int): List<RecentGameDto> = coroutineScope{
        val list = mutableListOf<RecentGameDto>()
        async {
            database.connection?.use { conn ->
                val statement = conn.prepareStatement(getRecentGamesForUserSql)

                statement.apply {
                    setInt(1, userId)
                }

                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    list.add(
                        Game(
                            id = resultSet.getInt("game_id"),
                            name = resultSet.getString("name"),
                            scoringMethodId = resultSet.getInt("scoring_method_id"),
                            description = resultSet.getString("description"),
                            publisher = resultSet.getString("publisher"),
                            // getInt() returns 0 if value is SQL NULL, so we need to make sure it's mapped back to null
                            minPlayers = resultSet.getInt("minimum_player_count").let { if (it > 0) it else null },
                            maxPlayers = resultSet.getInt("maximum_player_count").let { if (it > 0) it else null },
                            duration = resultSet.getInt("duration").let { if (it > 0) it else null },
                            minAge = resultSet.getInt("minimum_age").let { if (it > 0) it else null },
                            releaseDate = resultSet.getDate("release_date")?.toLocalDate()?.toKotlinLocalDate(),
                        ).withLastPlayed(
                            resultSet.getDate("achieved_on").toLocalDate().toKotlinLocalDate()
                        )
                    )
                }
            }
            list
        }.await()
    }
}
