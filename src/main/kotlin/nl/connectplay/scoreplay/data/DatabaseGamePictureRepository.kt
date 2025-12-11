package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.GamePictureRepository
import java.util.*

class DatabaseGamePictureRepository(private val database: Database) : GamePictureRepository {
    override suspend fun addGamePicture(gameId: Int, pictureId: UUID): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val sql =
                        """
                            INSERT INTO game_pictures (game_id, picture_id) VALUES (?, ?)
                        """.trimIndent()
                    val stmt = connection.prepareStatement(sql)

                    stmt.setInt(1, gameId)
                    stmt.setObject(2, pictureId)
                    stmt.executeUpdate()
                    stmt.close()
                    return@use true
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@use false
                }
            }
        }.await() ?: false
    }

    private val getGamePictureUrlsSql =
        """
        SELECT picture_url FROM pictures
        JOIN game_pictures ON pictures.picture_id = game_pictures.picture_id
        WHERE game_id = ?;
        """.trimIndent()

    override suspend fun getGamePictureUrls(gameId: Int): List<String> = coroutineScope {
        async {
            database.connection?.use { connection ->
                val stmt = connection.prepareStatement(getGamePictureUrlsSql)
                    .apply { setInt(1, gameId) }

                val resultSet = stmt.executeQuery()

                val urls = mutableListOf<String>()
                while (resultSet.next()) {
                    urls.add(resultSet.getString("picture_url"))
                }

                resultSet.close()
                stmt.close()

                urls
            }
        }.await() ?: listOf()
    }
}