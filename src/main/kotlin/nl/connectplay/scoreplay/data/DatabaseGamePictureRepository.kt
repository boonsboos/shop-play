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
                            INSERT INTO game_picture (game_id, picture_id) VALUES (?, ?)
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
}