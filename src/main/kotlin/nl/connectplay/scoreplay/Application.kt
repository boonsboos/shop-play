package nl.connectplay.scoreplay

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import nl.connectplay.scoreplay.routes.registerApplicationRoutes
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // configure serialization
    install(ContentNegotiation) {
        json()
    }

    install(SSE)

    install(CallLogging) {
        level = Level.INFO
    }

    // Use Koin dependency injection
    install(Koin) {
        slf4jLogger()
        modules(
            repositories(),
            database(),
            controllers(),
            services(),
            jwtOptions(this@module.environment.config), // pass in application configuration
        )
    }

    configureAuthentication()

    // Show API documentation on this path
    routing {
        openAPI(path = "openapi")
    }

    routing {
        registerApplicationRoutes()
    }
}