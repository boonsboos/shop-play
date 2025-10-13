package nl.connectplay.scoreplay.utilities

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import nl.connectplay.scoreplay.UserIdJWTClaim

/**
 * A utility function to streamline getting the user ID from the JWT authentication token.
 */
fun ApplicationCall.getUserIdFromJWT(): Int {
    // if the principal is unavailable, we cannot continue
    val principal = this.principal<JWTPrincipal>()
        ?: throw IllegalStateException("JWT Principal is invalid")

    // we already validated the user id is present
    return principal.payload.getClaim(UserIdJWTClaim).asInt()
}