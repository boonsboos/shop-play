package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDate
import nl.connectplay.scoreplay.abstraction.data.FollowGameRepository
import nl.connectplay.scoreplay.models.dto.GameDto
import nl.connectplay.scoreplay.models.dto.user.UserDto

class DatabaseFollowGameRepository(private val database: Database) : FollowGameRepository {
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
                        setInt(2, limit ?: 10)
                        setInt(3, offset)
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

    override suspend fun followGame(userId: Int, gameId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun unfollowGame(userId: Int, gameId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getFollowers(
        gameId: Int,
        offset: Int,
        limit: Int?
    ): List<UserDto> {
        TODO("Not yet implemented")
    }
}