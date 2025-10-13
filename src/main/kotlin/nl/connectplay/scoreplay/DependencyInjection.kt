package nl.connectplay.scoreplay

import nl.connectplay.scoreplay.abstraction.data.ExampleRepository
import nl.connectplay.scoreplay.abstraction.data.SessionRepository
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.controllers.ExampleController
import nl.connectplay.scoreplay.controllers.SessionController
import nl.connectplay.scoreplay.controllers.UserController
import nl.connectplay.scoreplay.controllers.GameController
import nl.connectplay.scoreplay.data.Database
import nl.connectplay.scoreplay.data.DatabaseExampleRepository
import nl.connectplay.scoreplay.data.DatabaseSessionRepository
import nl.connectplay.scoreplay.data.DatabaseUserRepository
import nl.connectplay.scoreplay.data.DatabaseGameRepository
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

fun database() = module {
    singleOf(::Database) {
        createdAtStart() // make sure our database is available directly when we start receiving requests
        onClose { it?.close() } // clean up on program shutdown
    }
}