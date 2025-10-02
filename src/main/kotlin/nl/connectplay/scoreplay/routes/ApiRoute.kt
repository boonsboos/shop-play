package nl.connectplay.scoreplay.routes

/**
 * Marks a route function to be included in the route registry.
 * By adding this annotation to a function, the route will be made a part of the API at runtime.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiRoute
