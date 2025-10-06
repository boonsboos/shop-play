package nl.connectplay.scoreplay.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExampleRepository {

    val connection = Database().connection

    /**
     * Runs an example database query asynchronously
     */
    suspend fun getExampleAsync(): Boolean? {
        // if you want to get a result back, you need to start a new coroutine
        return coroutineScope {
            // run your query asynchronously (without blocking the current thread)
            // this means that while we are communicating with the database,
            // Ktor can handle other requests in the background
            val databaseResult = async {
                val statement = connection?.prepareStatement("SELECT user_id FROM users")
                val resultSet = statement?.executeQuery()

                resultSet?.last() // we want to know if we are on the last row of the result set
            }
            // await and return the result from the coroutine
            databaseResult.await()
        }
    }
}