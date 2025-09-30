package nl.connectplay.shopplay.routes

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.exampleRoutes() {
    routing {
        get("/example") {
            call.respond("Example!")
        }
    }
}