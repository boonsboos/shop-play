package nl.connectplay.scoreplay.models

import java.util.UUID

data class User(val id: Int, val username: String, val email: String, val passwordHash: String, val pictureId: UUID?)