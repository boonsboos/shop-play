package nl.connectplay.scoreplay

import io.ktor.server.config.*
import nl.connectplay.scoreplay.abstraction.data.*
import nl.connectplay.scoreplay.abstraction.services.*
import nl.connectplay.scoreplay.controllers.*
import nl.connectplay.scoreplay.data.*
import nl.connectplay.scoreplay.events.EventQueueManagerServiceImpl
import nl.connectplay.scoreplay.events.EventRouter
import nl.connectplay.scoreplay.options.DBOptions
import nl.connectplay.scoreplay.options.JWTOptions
import nl.connectplay.scoreplay.services.*
import org.koin.core.module.dsl.bind
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
    singleOf(::DatabaseScoreRepository) { bind<ScoreRepository>() }
    singleOf(::DatabasePictureRepository) { bind<PictureRepository>() }
    singleOf(::DatabaseGamePictureRepository) { bind<GamePictureRepository>() }
    singleOf(::DatabaseNotificationRepository) { bind<NotificationRepository>() }
    singleOf(::DatabaseFollowGameRepository) { bind<FollowGameRepository>() }
    singleOf(::DatabaseLeaderboardRepository) { bind<LeaderboardRepository>() }
}

/**
 * Register controllers
 */
fun controllers() = module {
    singleOf(::ExampleController)
    singleOf(::SessionController)
    singleOf(::UserController)
    singleOf(::ScoreController)
    singleOf(::GameController)
    singleOf(::NotificationController)
    singleOf(::PictureController)
    singleOf(::LeaderboardController)
}

/**
 * Register services
 */
fun services() = module {
    singleOf(::FriendServiceImpl) { bind<FriendService>() }
    singleOf(::UserAccountServiceImpl) { bind<UserAccountService>() }
    singleOf(::PictureServiceImpl) { bind<PictureService>() }
    singleOf(::ScoreServiceImpl) { bind<ScoreService>() }
    singleOf(::SessionServiceImpl) { bind<SessionService>() }

    // events
    singleOf(::EventRouter) { bind<EventRoutingService>() }
    singleOf(::EventQueueManagerServiceImpl) { bind<EventQueueManagerService>() }
}

/**
 * Set the global application configuration for options classes
 */
fun options(config: ApplicationConfig) = module {
    single<JWTOptions> {
        JWTOptions(
            config.property("jwt.secret").getString(),
            config.property("jwt.issuer").getString(),
            config.property("jwt.audience").getString(),
            config.property("jwt.realm").getString(),
        )
    }
    single<DBOptions> {
        DBOptions(
            config.property("db.url").getString(),
        )
    }
}

fun database() = module {
    single<Database>(createdAtStart = true) { Database(get<DBOptions>()) }
}