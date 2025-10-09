package nl.connectplay.scoreplay.controllers

import io.ktor.server.application.ApplicationCall
import io.ktor.util.reflect.typeInfo
import nl.connectplay.scoreplay.abstraction.data.ExampleRepository

/**
 * Controller for demonstrative purposes
 */
class ExampleController(private val exampleRepository: ExampleRepository) {

    /**
     * Handles the example endpoint asynchronously
     */
    suspend fun handleExampleAsync(call: ApplicationCall) {
        // respond to the call to our application
        call.respond(
            exampleRepository.getExampleAsync(),
            typeInfo<Boolean?>() // tell ktor that yes, we want to return a nullable object
        )
    }

}