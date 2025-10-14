package nl.connectplay.scoreplay.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import nl.connectplay.scoreplay.UserIdJWTClaim
import nl.connectplay.scoreplay.abstraction.data.UserRepository
import nl.connectplay.scoreplay.abstraction.services.UserAccountService
import nl.connectplay.scoreplay.exceptions.NotFoundException
import nl.connectplay.scoreplay.exceptions.UnauthorizedException
import nl.connectplay.scoreplay.models.dto.LoginUserDto
import nl.connectplay.scoreplay.options.JWTOptions
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

class UserAccountServiceImpl(private val userRepository: UserRepository, private val jwtConfiguration: JWTOptions) : UserAccountService {
    /**
     * Tries to log a user in.
     * @return a valid JWT token
     * @throws NotFoundException when user has not been found
     * @throws com.auth0.jwt.exceptions.JWTCreationException when JWT is somehow invalidated
     * @throws UnauthorizedException when passwords do not match
     */
    override suspend fun loginAsync(loginUserDto: LoginUserDto): String {
        val user = userRepository.getUserByNameOrEmail(loginUserDto.username, loginUserDto.email)
            ?: throw NotFoundException("No user account found with username ${loginUserDto.username} or email ${loginUserDto.email}")

        if(!BCrypt.checkpw(loginUserDto.password, user.passwordHash)) {
            throw UnauthorizedException("Password mismatch for user ${user.id}")
        }

        return JWT.create()
            .withAudience(jwtConfiguration.audience)
            .withIssuer(jwtConfiguration.issuer)
            .withClaim(UserIdJWTClaim, user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000))) // valid for 24 hours
            .sign(Algorithm.HMAC256(jwtConfiguration.secret))
    }
}