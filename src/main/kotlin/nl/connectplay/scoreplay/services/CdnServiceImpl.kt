package nl.connectplay.scoreplay.services

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.io.readByteArray
import nl.connectplay.scoreplay.abstraction.services.CdnService
import nl.connectplay.scoreplay.exceptions.NoFileUploadedException
import nl.connectplay.scoreplay.exceptions.TooManyFilesUploadedException
import nl.connectplay.scoreplay.exceptions.FileUploadFailedException
import nl.connectplay.scoreplay.models.dto.cdn.ImageResponseDto
import nl.connectplay.scoreplay.options.CDNOptions

class CdnServiceImpl(private val options: CDNOptions) : CdnService {
    override suspend fun uploadImage(multipart: MultiPartData): String = coroutineScope {
        async {
            // create an http client instance
            val client = HttpClient()
            var fileCount = 0

            var imageFile: PartData.FileItem? = null
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileCount++
                    if (fileCount == 1) imageFile = part
                }
                part.dispose()
            }

            if (fileCount == 0 || imageFile == null) throw NoFileUploadedException()
            if (fileCount > 1) throw TooManyFilesUploadedException()

            val bytes = imageFile.provider().readRemaining().readByteArray()

            val formData = formData {
                append(
                    "image",
                    bytes,
                    headers {
                        append(HttpHeaders.ContentType, imageFile.contentType.toString())
                        append(HttpHeaders.ContentDisposition, "filename=\"${imageFile.originalFileName}\"")
                    })
            }

            val res: HttpResponse = client.post(makeRequest("/cdn/upload/image")) {
                accept(ContentType.Application.Json)
                setBody(MultiPartFormDataContent(formData))
            }

            if (!res.status.isSuccess()) throw FileUploadFailedException(res.status.toString())

            val body = res.body<ImageResponseDto>()

            body.fileUrl
        }.await()
    }


    private fun makeRequest(path: String): String {
        return getUrl() + if (path.startsWith("/")) path else "/$path"
    }

    private fun getUrl(): String {
        return "${options.baseUrl}${options.basePath}"
    }
}