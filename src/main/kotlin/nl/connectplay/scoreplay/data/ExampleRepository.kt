package nl.connectplay.scoreplay.data

import io.ktor.server.response.respond
import javax.xml.crypto.Data

class ExampleRepository {

    val connection = Database().connection

    fun getExample(): Boolean? {
        val statement = connection?.prepareStatement("SELECT user_id FROM users")
        val result = statement?.executeQuery()

        return result?.last()
    }
}