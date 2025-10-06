package nl.connectplay.scoreplay.controllers

import io.ktor.server.application.ApplicationCall
import io.ktor.util.reflect.typeInfo
import nl.connectplay.scoreplay.data.ExampleRepository

class ExampleController {

    /**
     * Handles the example endpoint asynchronously
     */
    suspend fun handleExampleAsync(call: ApplicationCall) {
        val exampleRepository = ExampleRepository()

        // respond to the call to our application
        call.respond(
            exampleRepository.getExampleAsync(),
            typeInfo<Boolean?>() // tell ktor that yes, we want to return a nullable object
        )
    }

}