package nl.connectplay.scoreplay.routes.examples

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.ExampleController
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.exampleRoute() {
    val exampleController = ExampleController()

    get("/example") {
        // delegate handling this call to the ExampleController
        exampleController.handleExampleAsync(call) // call is an implicit variable referring to the HTTP call
    }
}
