package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDate
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.GameDto

class DatabaseGameRepository(private val database: Database) : GameRepository {

    override suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>? = coroutineScope {
        async {
            // SQL Statement to db
            database.connection?.use { conn ->
                val sql = """
                    SELECT 
                        g.game_id, g.name, g.description, g.publisher,
                        g.min_players, g.max_players, g.duration_minutes, g.min_age, g.release_date
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
                                    // getInt() returns 0 if value is SQL NULL, so we need to make sure it's mapped back to null
                                    minPlayers = rs.getInt("min_players").let { if (it > 0) it else null },
                                    maxPlayers = rs.getInt("max_players").let { if (it > 0) it else null },
                                    duration = rs.getInt("duration_minutes").let { if (it > 0) it else null },
                                    minAge = rs.getInt("min_age").let { if (it > 0) it else null },
                                    releaseDate = rs.getDate("release_date")?.toLocalDate()?.toKotlinLocalDate(),
                                )
                            )
                        }
                        list.toList() // can be empty
                    }
                }
            }
        }.await()
    }

    override suspend fun getGameByIdAsync(gameId: Int): GameDto? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                    SELECT game_id, name, description, publisher, min_players, max_players, duration_minutes, min_age, release_date
                    FROM games
                    WHERE game_id = ?
                """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setInt(1, gameId)

                val resultSet = stmt?.executeQuery()
                var game: GameDto? = null;
                if (resultSet?.next() == true) {
                    game = GameDto(
                        id = resultSet.getInt("game_id"),
                        name = resultSet.getString("name"),
                        description = resultSet.getString("description"),
                        publisher = resultSet.getString("publisher"),
                        minPlayers = resultSet.getInt("min_players").let { if (it > 0) it else null },
                        maxPlayers = resultSet.getInt("max_players").let { if (it > 0) it else null },
                        duration = resultSet.getInt("duration_minutes").let { if (it > 0) it else null },
                        minAge = resultSet.getInt("min_age").let { if (it > 0) it else null },
                        releaseDate = resultSet.getDate("release_date")?.toLocalDate()?.toKotlinLocalDate(),
                    )
                }

                stmt?.close()
                resultSet?.close()

                game
            }
        }.await()
    }
}
