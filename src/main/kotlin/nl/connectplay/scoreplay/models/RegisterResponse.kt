import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val data: UserDto? = null,
    val message: String? = null
)
