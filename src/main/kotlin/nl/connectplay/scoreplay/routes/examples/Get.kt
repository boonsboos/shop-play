package nl.connectplay.scoreplay.routes.examples

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.ExampleController
import nl.connectplay.scoreplay.routes.ApiRoute

@ApiRoute
fun Route.exampleRoute() {
    val exampleController = ExampleController()

    get("/example") {
        exampleController.handleExample(call)
    }
}
