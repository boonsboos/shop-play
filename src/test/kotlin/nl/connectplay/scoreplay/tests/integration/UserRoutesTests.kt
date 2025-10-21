package nl.connectplay.scoreplay.tests.integration

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import nl.connectplay.scoreplay.module
import org.junit.jupiter.api.Tag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserRoutesTests {
    @Test // marks this function as a test
    fun testRegisterUser() {
        testApplication { // runs the Ktor test environment
            environment { configure("application.yaml") }
            application { module() } // boot the real app: JSON + routes via annotation scanner

            // ARRANGE
            val firstResponse = client.post("/register") { // send a POST request to the /register endpoint
                setBody("""{"id":"1","username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""") // the is what the receiver wants call.receive<Map<String, String>>()
                header(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json
                ) // tells the server where sending Json data
            }

            // ASSERT (1)
            assertEquals(HttpStatusCode.Companion.Created, firstResponse.status)

            // ACT
            // check if user exist
            val secondResponse = client.post("/register") {
                setBody("""{"id":"2","username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            // ASSERT (2)
            assertEquals(HttpStatusCode.Companion.Conflict, secondResponse.status)
        }
    }

    @Test
    fun testUpdateUser() {
        testApplication {
            environment { configure("application.yaml") }
            application { module() }

            // ARRANGE
            client.post("/register") {
                setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            // ACT
            val patchResponse = client.patch("/users/1") {
                setBody("""{"username":"Luigi","email":"luigi@connect-play.nl"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            // ASSERT
            // check if the update was successful
            assertEquals(HttpStatusCode.Companion.OK, patchResponse.status)

            // check if the response data match
            val responseBody = patchResponse.bodyAsText()
            assertTrue(responseBody.contains("Luigi"))
            assertTrue(responseBody.contains("luigi@connect-play.nl"))
        }
    }

    @Test
    fun testDeleteUser() {
        testApplication {
            environment { configure("application.yaml") }
            application { module() }
            client.post("/register") {
                setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            val deleteResponse = client.delete("/users/1")
            assertEquals(HttpStatusCode.Companion.OK, deleteResponse.status) // check if the user was deleted

            val responseBody = deleteResponse.bodyAsText()
            assertTrue(responseBody.contains("Account deleted successfully"))
        }
    }
}