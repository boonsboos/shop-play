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
                    if (create.minPlayers != null) stmt.setInt(4, create.minPlayers) else stmt.setNull(4, java.sql.Types.INTEGER)
                    if (create.maxPlayers != null) stmt.setInt(5, create.maxPlayers) else stmt.setNull(5, java.sql.Types.INTEGER)
                    if (create.duration != null) stmt.setInt(6, create.duration) else stmt.setNull(6, java.sql.Types.INTEGER)
                    if (create.minAge != null) stmt.setInt(7, create.minAge) else stmt.setNull(7, java.sql.Types.INTEGER)

                    if (create.releaseDate != null) {
                        // kotlinx.datetime.LocalDate -> java.time.LocalDate -> java.sql.Date
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
}
