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
        database.connection?.use { connection -> // use automatically close resources or cleanup after completion
            val sql = """
                        INSERT INTO game_followers (user_id, game_id) VALUES (?, ?)
                    """.trimIndent()

            val stmt = connection.prepareStatement(sql)
            stmt.apply {
                setInt(1, userId)
                setInt(2, gameId)
            }

            stmt.executeUpdate()
        }
    }

    override suspend fun unfollowGame(userId: Int, gameId: Int) {
        database.connection?.use { connection -> // use automatically close resources or cleanup after completion
            val sql = """
                DELETE FROM game_followers
                WHERE user_id = ? AND game_id = ?
            """.trimIndent()

            val stmt = connection.prepareStatement(sql)
            stmt.apply {
                setInt(1, userId)
                setInt(2, gameId)
            }

            stmt.executeUpdate()
        }
    }

    override suspend fun getFollowers(gameId: Int, offset: Int, limit: Int?): List<UserDto> {
        return coroutineScope {
            async {
                database.connection?.use { connection ->
                    val sql = """
                    SELECT users.user_id AS user_id,
                           users.user_name,
                           users.email,
                           users.profile_picture
                    FROM game_followers
                    JOIN users ON game_followers.user_id = users.user_id
                    WHERE game_followers.game_id = ?
                    LIMIT ? OFFSET ?
                """.trimIndent()

                    val stmt = connection.prepareStatement(sql)

                    stmt.apply {
                        setInt(1, gameId)
                        setInt(2, limit ?: 10)
                        setInt(3, offset)
                    }

                    val resultSet = stmt.executeQuery()
                    val users = mutableListOf<UserDto>()

                    while (resultSet.next()) {
                        users.add(
                            UserDto(
                                username = resultSet.getString("user_name"),
                                email = resultSet.getString("email"),
                                profilePicture = resultSet.getString("profile_picture")
                            )
                        )
                    }
                    return@use users
                } ?: emptyList()
            }.await()
        }
    }

    override suspend fun getAllFollowerUserIdsAsync(gameId: Int): List<Int> = coroutineScope{
        val followerUserIds = mutableListOf<Int>()
        async {
            database.connection?.use {
                val statement = it.prepareStatement("""
                    SELECT user_id
                    FROM game_followers
                    WHERE game_id = ?
                """.trimIndent())

                statement.setInt(1, gameId)

                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    followerUserIds.add(
                        resultSet.getInt("user_id")
                    )
                }

                resultSet.close()
                statement.close()
            }
        }.await()

        followerUserIds.toList()
    }
}