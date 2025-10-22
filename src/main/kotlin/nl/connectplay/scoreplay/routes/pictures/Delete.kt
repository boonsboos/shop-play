package nl.connectplay.scoreplay.routes.pictures

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import nl.connectplay.scoreplay.controllers.PictureController
import nl.connectplay.scoreplay.routes.ApiRoute
import org.koin.ktor.ext.inject
import kotlin.getValue

@ApiRoute
fun Route.deletePicture() {
    val pictureController by inject<PictureController>()

    delete("/pictures/{id}"){
        pictureController.handleDeletePictureAsync(call)
    }

}