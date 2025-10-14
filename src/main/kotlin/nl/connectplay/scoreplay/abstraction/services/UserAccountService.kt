package nl.connectplay.scoreplay.abstraction.services

import nl.connectplay.scoreplay.models.dto.LoginUserDto

interface UserAccountService {
    /**
     * Tries to log a user in
     */
    suspend fun loginAsync(loginUserDto: LoginUserDto): String
}