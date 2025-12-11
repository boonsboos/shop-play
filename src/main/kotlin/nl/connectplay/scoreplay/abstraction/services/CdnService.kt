package nl.connectplay.scoreplay.abstraction.services

import io.ktor.client.statement.*
import io.ktor.http.content.*

interface CdnService {
    suspend fun forwardImage(formData: PartData.FileItem): HttpResponse
}
