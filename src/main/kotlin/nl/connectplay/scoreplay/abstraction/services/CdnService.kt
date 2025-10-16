package nl.connectplay.scoreplay.abstraction.services

import io.ktor.http.content.MultiPartData

interface CdnService {
    suspend fun uploadImage(multipart: MultiPartData): String
}