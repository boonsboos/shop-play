package nl.connectplay.scoreplay.models.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateDto(val username: String?, val password: String? = null, val email: String?) // the ? is for optional