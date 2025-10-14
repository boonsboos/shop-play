package nl.connectplay.scoreplay

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.routing.routing
import nl.connectplay.scoreplay.routes.registerApplicationRoutes
import org.slf4j.event.Level
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

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

    // Use Koin dependency injection
    install(Koin) {
        slf4jLogger()
        modules(
            repositories(),
            database(),
            controllers(),
            services(),
            jwtOptions(this@module.environment.config) // pass in application configuration
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