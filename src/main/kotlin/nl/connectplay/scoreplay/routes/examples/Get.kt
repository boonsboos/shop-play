package nl.connectplay.scoreplay.routes.examples

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.exampleRoute() {
    get("/example") {
        call.respond(mapOf("hello" to "world"))
    }
}
