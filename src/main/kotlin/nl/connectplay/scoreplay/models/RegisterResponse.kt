import kotlinx.serialization.Serializable
import nl.connectplay.scoreplay.models.dto.user.UserDto

@Serializable
data class RegisterResponse(
    val data: UserDto? = null,
    val message: String? = null
)
