package nl.connectplay.scoreplay.models.dto.user

import kotlinx.serialization.Serializable

@Serializable
/**
 * A DTO for users to log in.
 * @param username the username of the user
 * @param email the email of the user
 * @param password the raw password of the user
 */
// keep username and email nullable in case the user wants to log in with either
data class LoginUserDto(val username: String? = null, val email: String? = null, val password: String)