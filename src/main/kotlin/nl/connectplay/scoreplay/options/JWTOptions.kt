package nl.connectplay.scoreplay.options

data class JWTOptions(val secret: String, val issuer: String, val audience: String, val realm: String)
