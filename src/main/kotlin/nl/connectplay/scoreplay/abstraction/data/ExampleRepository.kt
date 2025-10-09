package nl.connectplay.scoreplay.abstraction.data

/**
 * Abstraction for the example repository
 */
interface ExampleRepository {
    suspend fun getExampleAsync(): Boolean?
}