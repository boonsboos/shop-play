package nl.connectplay.scoreplay

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.routing.routing
import nl.connectplay.scoreplay.routes.registerApplicationRoutes
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // configure serialization
    install(ContentNegotiation) {
        json()
    }

    install(CallLogging) {
        level = Level.INFO
    }

    dependencies {
        // define database connection providers/repositories here
    }

    // Show API documentation on this path
    routing {
        openAPI(path = "openapi")
    }

    routing {
        registerApplicationRoutes()
    }
}
