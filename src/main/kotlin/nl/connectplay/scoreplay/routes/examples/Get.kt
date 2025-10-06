package nl.connectplay.scoreplay.routes.examples

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.util.reflect.typeInfo
import nl.connectplay.scoreplay.controllers.ExampleController
import nl.connectplay.scoreplay.data.Database
import nl.connectplay.scoreplay.data.ExampleRepository
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.exampleRoute() {
    val exampleController = ExampleController()

    get("/example") {
        exampleController.handleExample(call)
    }
}
