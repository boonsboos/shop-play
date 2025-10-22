package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.dto.user.LoginUserDto

interface UserAccountService {
    /**
     * Tries to log a user in
     */
    suspend fun loginAsync(loginUserDto: LoginUserDto): String
}