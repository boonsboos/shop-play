package nl.connectplay.scoreplay.tests.nl.connectplay.scoreplay.tests

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import nl.connectplay.scoreplay.module
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/example").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}