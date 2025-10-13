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

    // if the claim cannot be converted to an int, the function also returns null and we throw an exception
    return principal.payload.getClaim(UserIdJWTClaim).asInt()
}