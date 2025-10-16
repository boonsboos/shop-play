package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import nl.connectplay.scoreplay.abstraction.data.PictureRepository
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
}