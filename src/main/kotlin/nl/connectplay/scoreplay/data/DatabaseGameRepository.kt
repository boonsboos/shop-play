package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.GameDto
import java.sql.Date

class DatabaseGameRepository(private val database: Database) : GameRepository {

    override suspend fun getGamesAsync(limit: Int?, offset: Int?, query: String?): List<GameDto>? = coroutineScope {
        async {
            database.connection?.use { conn ->
                val sql = """
                    SELECT 
                        g.game_id, g.name, g.description, g.publisher,
                        g.min_players, g.max_players, g.duration_minutes, g.min_age, g.release_date
                    FROM games g
                    WHERE (? IS NULL OR g.name LIKE ? OR g.publisher LIKE ?)
                    ORDER BY g.game_id
                    LIMIT ? OFFSET ?
                """.trimIndent()

                conn.prepareStatement(sql).use { stmt ->
                    // param 1 voor IS NULL check
                    stmt.setString(1, query)
                    // LIKE params
                    val like = "%${query ?: ""}%"
                    stmt.setString(2, like)
                    stmt.setString(3, like)
                    stmt.setInt(4, limit ?: 25)
                    stmt.setInt(5, offset ?: 0)

                    stmt.executeQuery().use { rs ->
                        val list = mutableListOf<GameDto>()
                        while (rs.next()) {
                            list.add(
                                GameDto(
                                    id = rs.getInt("game_id"),
                                    name = rs.getString("name"),
                                    description = rs.getString("description"),
                                    publisher = rs.getString("publisher"),
                                    minPlayers = rs.getInt("min_players").let { if (rs.wasNull()) null else it },
                                    maxPlayers = rs.getInt("max_players").let { if (rs.wasNull()) null else it },
                                    duration = rs.getInt("duration_minutes").let { if (rs.wasNull()) null else it },
                                    minAge = rs.getInt("min_age").let { if (rs.wasNull()) null else it },
                                    releaseDate = rs.getDate("release_date")?.let { kotlinx.datetime.LocalDate.parse(it.toLocalDate().toString()) }
                                )
                            )
                        }
                        list.toList() // kan leeg zijn
                    }
                }
            }
        }.await()
    }
}
