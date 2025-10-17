package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDate
import nl.connectplay.scoreplay.abstraction.data.GameRepository
import nl.connectplay.scoreplay.models.dto.GameDto
import kotlinx.datetime.LocalDate
import java.lang.reflect.Array.setInt

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
                                    minPlayers = rs.getInt("min_players").let { if (it > 0 ) it else null },
                                    maxPlayers = rs.getInt("max_players").let { if (it > 0 ) it else null },
                                    duration = rs.getInt("duration_minutes") .let { if (it > 0 ) it else null },
                                    minAge = rs.getInt("min_age").let { if (it > 0 ) it else null },
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

    override suspend fun getFollowedGames(userId: Int, offset: Int, limit: Int?): List<GameDto> {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val sql = """
                        SELECT games.game_id AS game_id,
                               games.name,
                               games.description,
                               games.publisher,
                               games.minimum_player_count,
                               games.maximum_player_count,
                               games.duration,
                               games.minimum_age,
                               games.release_date
                        FROM game_followers
                        JOIN games ON game_followers.game_id = games.game_id
                        WHERE game_followers.user_id = ?
                        LIMIT ? OFFSET ?
                    """.trimIndent()


                    val stmt = connection.prepareStatement(sql)
                    // with the apply block you dont have to define stmt multiple times
                    stmt.apply {
                        setInt(1, userId)
                        setInt(3, offset)
                        setInt(2, limit ?: 10)
                    }

                    // returned rows result
                    val resultSet = stmt?.executeQuery()

                    val games = mutableListOf<GameDto>()

                    while (resultSet?.next() == true) {
                        val gameId = resultSet.getInt("game_id") // because of the ALIAS its game_id not games_id
                        val name = resultSet.getString("name")
                        val description = resultSet.getString("description")
                        val publisher = resultSet.getString("publisher")
                        val minPlayers = resultSet.getInt("minimum_player_count")
                        val maxPlayers = resultSet.getInt("maximum_player_count")
                        val duration = resultSet.getInt("duration")
                        val minAge = resultSet.getInt("minimum_age")
                        // converts the java.sql.Date object to java.time.LocalDate(modern date type)
                        // then converts it to a kotlinx.datetime.LocalDate for in the Dto
                        val releaseDate = resultSet.getDate("release_date")
                            ?.toLocalDate()?.toKotlinLocalDate()

                        // create gamesDto
                        games.add(
                            GameDto(
                                id = gameId,
                                name = name,
                                description = description,
                                publisher = publisher,
                                minPlayers = minPlayers,
                                maxPlayers = maxPlayers,
                                duration = duration,
                                minAge = minAge,
                                releaseDate = releaseDate
                            )
                        )
                    }
                    // return@use ensures the games list is returned from this use block
                    return@use games
                } ?: emptyList() // <-- ensure non-null
            }.await()
        }
    }
}