package nl.connectplay.scoreplay.routes

import io.github.classgraph.ClassGraph
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

fun Application.registerApplicationRoutes() {

    val existingRoutes = getRouteAnnotatedFunctions()

    println(existingRoutes)

    routing {
        existingRoutes.forEach {
            it.call(this) // call
        }
    }
}

private const val routesPackage: String = "nl.connectplay.scoreplay.routes"

// Adapted from https://stackoverflow.com/a/69215277
/**
 * @return List of functions that return routes
 */
private fun getRouteAnnotatedFunctions(): List<KFunction<*>> {
    val annotation = ApiRoute::class.java
    val annotationName = annotation.canonicalName

    // Gets all methods that use the ApiRoute annotation
    return ClassGraph()
        .enableAllInfo()
        .acceptPackages(routesPackage)
        .scan().use { scanResult ->
            scanResult.getClassesWithMethodAnnotation(annotationName).flatMap { inferredRouteMethod ->
                inferredRouteMethod.methodInfo.filter { function ->
                    function.hasAnnotation(annotation)
                }.mapNotNull { method ->
                        method.loadClassAndGetMethod().kotlinFunction
                    }
            }
        }
}