package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.PictureRepository
import nl.connectplay.scoreplay.models.dto.picture.PictureDto
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

class DatabasePictureRepository(private val database: Database) : PictureRepository {
    override suspend fun addImageAsync(url: String): UUID? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                        INSERT INTO pictures (picture_url) VALUES (?) RETURNING picture_id
                    """.trimIndent()

                val stmt = connection.prepareStatement(sql)

                stmt.setString(1, url)
                val resultSet = stmt.executeQuery()

                val id = if (resultSet.next()) resultSet.getObject("picture_id", UUID::class.java) else null

                resultSet.close()
                stmt.close()

                id
            }
        }.await()
    }

    override suspend fun getPictureById(pictureId: UUID): String? = coroutineScope {
        async {
            database.connection?.use { connection ->
                val sql = """
                    SELECT picture_url FROM pictures
                    WHERE picture_id = ?
                """.trimIndent()

                val stmt = connection.prepareStatement(sql)
                stmt.setObject(1, pictureId)

                val resultSet = stmt.executeQuery()

                val id = if (resultSet.next()) resultSet.getString("picture_url") else null

                resultSet.close()
                stmt.close()

                id
            }
        }.await()
    }

    override suspend fun deletePictureById(pictureId: UUID): Boolean = coroutineScope {
        async {
            database.connection?.use { connection ->
                try {
                    val sql = """
                        DELETE FROM pictures WHERE picture_id = ?
                    """.trimIndent()

                    val stmt = connection.prepareStatement(sql)
                    stmt.setObject(1, pictureId)

                    val affectedRow = stmt.executeUpdate()
                    stmt.close()
                    return@async affectedRow > 0

                } catch (e: SQLException) {
                    e.printStackTrace()
                    return@async false
                }
            } ?: false
        }.await()
    }
}