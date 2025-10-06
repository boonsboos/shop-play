package nl.connectplay.scoreplay.routes.examples

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.data.Database
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.exampleRoute() {
    get("/example") {
        Database().connection?.use { connection ->

            val statement = connection.prepareStatement("SELECT user_id FROM users")
            val result = statement.executeQuery()

            call.respond(result?.last() ?: false)

            connection.close()
        }
    }
}
