package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.CreateGameDto
import nl.connectplay.scoreplay.models.dto.GameDto
import nl.connectplay.scoreplay.models.dto.UpdateGameDto
import java.sql.Date
import java.sql.Statement

class DatabaseGameRepository(private val database: Database) : GameRepository {

    override suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>? = coroutineScope {
        async {
            // SQL Statement to db
            database.connection?.use { conn ->
                val sql = """
                    SELECT 
                        game_id, name, description, publisher,
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
                        val list = mutableListOf<GameDto>()
                        while (rs.next()) {
                            list.add(
                                GameDto(
                                    id = rs.getInt("game_id"),
                                    name = rs.getString("name"),
                                    description = rs.getString("description"),
                                    publisher = rs.getString("publisher"),
                                    // getInt() returns 0 if value is SQL NULL
                                    minPlayers = rs.getInt("minimum_player_count").let { if (it > 0 ) it else null },
                                    maxPlayers = rs.getInt("maximum_player_count").let { if (it > 0 ) it else null },
                                    duration = rs.getInt("duration") .let { if (it > 0 ) it else null },
                                    minAge = rs.getInt("minimum_age").let { if (it > 0 ) it else null },
                                    releaseDate = rs.getDate("release_date")?.toLocalDate()?.toKotlinLocalDate(),
                                )
                            )
                        }
                        list.toList() // Can be empty
                    }
                }
            }
        }.await()
    }

    override suspend fun addGame(create: CreateGameDto): GameDto = coroutineScope {
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
                    stmt.setInt(9, 1) // TODO: propagate from model
                    stmt.executeUpdate()

                    // Read generated id
                    val rs = stmt.generatedKeys
                    val generatedId = if (rs.next()) rs.getInt(1) else throw IllegalStateException("Failed to retrieve generated id")
                    rs.close()

                    // Return created GameDto (without re-query; use provided fields + id)
                    GameDto(
                        id = generatedId,
                        scoringMethodId = 1,
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

override suspend fun updateGame(id: Int, update: UpdateGameDto): GameDto? = coroutineScope {
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
                    release_date = COALESCE(?, release_date)
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
                stmt.setObject(8,  update.releaseDate?.let {
                    Date.valueOf(it.toJavaLocalDate())
                })
                stmt.setInt(9, id)

                val updated = stmt.executeUpdate()
                if (updated == 0) return@use null
            }

            null
        }
    }.await()
}

}
