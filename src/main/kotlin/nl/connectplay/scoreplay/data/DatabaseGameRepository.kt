package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDate
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.GameDto
import nl.connectplay.scoreplay.models.dto.CreateGameDto
import java.sql.Date
import java.sql.Statement

class DatabaseGameRepository(private val database: Database) : GameRepository {

    override suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>? = coroutineScope {
        async {
            // SQL Statement to db
            database.connection?.use { conn ->
                val sql = """
                    SELECT 
                        g.game_id, g.name, g.description, g.publisher,
                        g.minimum_player_count, g.maximum_player_count, g.duration, g.minimum_age, g.release_date
                    FROM games g
                    WHERE (? IS NULL OR g.name LIKE ? OR g.publisher LIKE ? OR g.description LIKE ?)
                    ORDER BY g.game_id
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
                      (name, description, publisher, minimum_player_count, maximum_player_count, duration, minimum_age, release_date)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()

                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setString(1, create.name)
                    stmt.setString(2, create.description)
                    stmt.setString(3, create.publisher)
                    stmt.setInt(4, create.minPlayers)
                    stmt.setInt(5, create.maxPlayers)
                    stmt.setInt(6, create.duration)
                    stmt.setInt(7, create.minAge)

                    if (create.releaseDate != null) {
                        // Convert kotlinx.datetime.LocalDate -> java.time.LocalDate -> java.sql.Date
                        val javaLocal = java.time.LocalDate.of(create.releaseDate.year, create.releaseDate.month, create.releaseDate.day)
                        stmt.setDate(8, Date.valueOf(javaLocal))
                    } else {
                        stmt.setNull(8, java.sql.Types.DATE)
                    }

                    stmt.executeUpdate()

                    // Read generated id
                    val rs = stmt.generatedKeys
                    val generatedId = if (rs.next()) rs.getInt(1) else throw IllegalStateException("Failed to retrieve generated id")
                    rs.close()

                    // Return created GameDto (without re-query; use provided fields + id)
                    GameDto(
                        id = generatedId,
                        scoringMethodId = null,
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
                stmt.setInt(1, id)
                stmt.setString(1, update.name)
                stmt.setString(2, update.description)
                stmt.setString(3, update.publisher)
                stmt.setInt(4, update.minPlayers)
                stmt.setInt(5, update.maxPlayers)
                stmt.setInt(6, update.duration)
                stmt.setInt(7, update.minAge)
                stmt.setDate(8,  update.releaseDate?.let {
                    Date.valueOf(it.toLocalDate())
                })

                val updated = stmt.executeUpdate()
                if (updated == 0) return@use null
            }

            // Re-query updated row
            val selectSql = """
                SELECT 
                  g.game_id, g.name, g.description, g.publisher,
                  g.minimum_player_count, g.maximum_player_count, g.duration, g.minimum_age, g.release_date
                FROM games g
                WHERE g.game_id = ?
            """.trimIndent()

            conn.prepareStatement(selectSql).use { selectStmt ->
                selectStmt.setInt(1, id)
                selectStmt.executeQuery().use { rs ->
                    if (rs.next()) mapRowToGameDto(rs) else null
                }
            }
        }
    }.await()
}

}
