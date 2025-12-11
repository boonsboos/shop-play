package nl.connectplay.scoreplay.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import nl.connectplay.scoreplay.abstraction.services.CdnService

class HttpCdnService : CdnService {
    private val cdnClient = HttpClient(CIO) {
        defaultRequest {
            url("http://cdn.connectplay.local/images")
        }
    }

    override suspend fun forwardImage(
        formData: PartData.FileItem,
    ): HttpResponse {
        try {
            val forwardableBody = MultiPartFormDataContent(formData {
                appendInput(key = "Game_Image", headers = Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"${formData.name ?: "Game_Image"}\"; filename=\"${formData.originalFileName ?: "Game_Image"}\""
                    )
                    append(HttpHeaders.ContentType, formData.contentType ?: ContentType.Image.JPEG)
                }) {
                    // read the file data to a buffered stream
                    formData.provider().asSource().buffered()
                }
            })

            return this.cdnClient.post {
                contentType(ContentType.MultiPart.FormData)
                setBody(forwardableBody)
            }
        } finally {
            // dispose the data
            formData.dispose()
        }
    }
}