package nl.connectplay.scoreplay

import io.ktor.server.config.*
import nl.connectplay.scoreplay.abstraction.data.*
import nl.connectplay.scoreplay.abstraction.services.EventQueueManagerService
import nl.connectplay.scoreplay.abstraction.services.EventRoutingService
import nl.connectplay.scoreplay.abstraction.services.FriendService
import nl.connectplay.scoreplay.abstraction.services.UserAccountService
import nl.connectplay.scoreplay.controllers.ExampleController
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.data.*
import nl.connectplay.scoreplay.events.EventQueueManagerServiceImpl
import nl.connectplay.scoreplay.events.EventRouter
import nl.connectplay.scoreplay.options.JWTOptions
import nl.connectplay.scoreplay.services.FriendServiceImpl
import nl.connectplay.scoreplay.services.UserAccountServiceImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.onClose
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Registers concrete repository instances with their abstraction
 */
fun repositories() = module {
    singleOf(::DatabaseSessionRepository) { bind<SessionRepository>() }
    singleOf(::DatabaseExampleRepository) { bind<ExampleRepository>() }
    singleOf(::DatabaseUserRepository) { bind<UserRepository>() }
    singleOf(::DatabaseGameRepository) { bind<GameRepository>() }
    singleOf(::DatabaseFriendRepository) { bind<FriendRepository>() }
    singleOf(::DatabaseNotificationRepository) { bind<NotificationRepository>() }
}

/**
 * Register controllers
 */
fun controllers() = module {
    singleOf(::ExampleController)
    singleOf(::SessionController)
    singleOf(::UserController)
    singleOf(::GameController)
}

/**
 * Register services
 */
fun services() = module {
    singleOf(::FriendServiceImpl) { bind<FriendService>() }
    singleOf(::UserAccountServiceImpl) { bind<UserAccountService>() }
    // events
    singleOf(::EventRouter) { bind<EventRoutingService>() }
    singleOf(::EventQueueManagerServiceImpl) { bind<EventQueueManagerService>() }
}

/**
 * Set the global application configuration for JWT tokens
 */
fun jwtOptions(config: ApplicationConfig) = module {
    single<JWTOptions> {
        JWTOptions(
            config.property("jwt.secret").getString(),
            config.property("jwt.issuer").getString(),
            config.property("jwt.audience").getString(),
            config.property("jwt.realm").getString(),
        )
    }
}

fun database() = module {
    singleOf(::Database) {
        createdAtStart() // make sure our database is available directly when we start receiving requests
        onClose { it?.close() } // clean up on program shutdown
    }
}