package nl.connectplay.scoreplay.data

class ExampleRepository {

    val connection = Database().connection

    fun getExample(): Boolean? {
        val statement = connection?.prepareStatement("SELECT user_id FROM users")
        val result = statement?.executeQuery()

        return result?.last()
    }
}