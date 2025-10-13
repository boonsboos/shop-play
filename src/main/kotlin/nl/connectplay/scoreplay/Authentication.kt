package nl.connectplay.scoreplay

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import nl.connectplay.scoreplay.options.JWTOptions
import org.koin.ktor.ext.inject

/**
 * The name of the JWT instance that is meant for users
 */
const val UserIdJWTAuthenticatorName = "jwt-users"

/**
 * The JWT claim for the user ID
 */
const val UserIdJWTClaim = "userId"

/**
 * Configures JWT authentication for the application
 *
 */
fun Application.configureAuthentication() {
//    val secret = environment.config.property("jwt.secret").getString()
//    val realm = environment.config.property("jwt.realm").getString()
//    val audience = environment.config.property("jwt.audience").getString()
//    val issuer = environment.config.property("jwt.issuer").getString()

    val jwtOptions by inject<JWTOptions>()

    authentication {
        jwt(UserIdJWTAuthenticatorName) {
            // "permission" scope.
            // for our use case, that's the entire API
            this.realm = jwtOptions.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtOptions.secret))
                    .withIssuer(jwtOptions.issuer)
                    .withAudience(jwtOptions.audience)
                    .build()
            )
            // validate the credential is correct
            validate { credential ->
                val user = credential.payload.getClaim(UserIdJWTClaim).asInt() ?: return@validate null // no user id is invalid
                val expiryTime = credential.expiresAt?.time ?: return@validate null // no expiry date is invalid

                // the user ID cannot be 0 or negative and the credential has to expire in the future
                if (user > 0 && expiryTime > System.currentTimeMillis()) {
                    JWTPrincipal(credential.payload) // we pass the credential to the route
                } else {
                    null
                }
            }
            // respond if the validation fails
            challenge { _, _ -> // ignore arguments
                call.respond(HttpStatusCode.Unauthorized, "Token is invalid")
            }
        }
    }
}