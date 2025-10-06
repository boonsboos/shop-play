package nl.connectplay.scoreplay.controllers

import io.ktor.server.application.ApplicationCall
import io.ktor.util.reflect.typeInfo
import nl.connectplay.scoreplay.data.ExampleRepository

class ExampleController {

    suspend fun handleExample(call: ApplicationCall) {
        val exampleRepository = ExampleRepository()

        call.respond(
            exampleRepository.getExample(),
            typeInfo<Boolean?>()
        )
    }

}