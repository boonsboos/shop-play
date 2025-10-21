package nl.connectplay.scoreplay.tests.integration

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import nl.connectplay.scoreplay.module
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserRoutesTests {
    @Test // marks this function as a test
    @Order(1)
    fun test1RegisterUserReservesUsernameAndEmail() = testApplication { // runs the Ktor test environment
        environment { configure("application.yaml") }
        application { module() } // boot the real app: JSON + routes via annotation scanner

        // ARRANGE
        val firstResponse = client.post("/register") { // send a POST request to the /register endpoint
            setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""") // the is what the receiver wants call.receive<Map<String, String>>()
            header(HttpHeaders.ContentType, ContentType.Application.Json) // tells the server we're sending Json data
        }

        // ASSERT (1)
        assertEquals(HttpStatusCode.Created, firstResponse.status)

        // ACT
        // check if user exist
        val secondResponse = client.post("/register") {
            setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // ASSERT (2)
        assertEquals(HttpStatusCode.Conflict, secondResponse.status)
    }

    @Test
    @Order(2)
    fun test2LoginAndUpdateUser() = testApplication {
        environment { configure("application.yaml") }
        application { module() }

        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // ARRANGE
        val loginResponse = client.post("/login") {
            setBody("""{"username": "Mario","password": "Welkom01"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        val tokenBody = loginResponse.body<Map<String, String>>()

        // ACT
        val patchResponse = client.patch("/users/1") {
            setBody("""{"username":"Luigi","email":"luigi@connect-play.nl"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${tokenBody["token"]}")
        }

        // ASSERT
        // check if the update was successful
        assertEquals(HttpStatusCode.OK, patchResponse.status)

        // check if the response data match
        val responseBody = patchResponse.bodyAsText()
        assertTrue(responseBody.contains("Luigi"))
        assertTrue(responseBody.contains("luigi@connect-play.nl"))
    }

    @Test
    @Order(3)
    fun test3LoginAndDeleteUser() = testApplication {
        environment { configure("application.yaml") }
        application { module() }

        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val loginResponse = client.post("/login") {
            setBody("""{"username": "Mario","password": "Welkom01"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        val tokenBody = loginResponse.body<Map<String, String>>()

        val deleteResponse = client.delete("/users/1") {
            header(HttpHeaders.Authorization, "Bearer ${tokenBody["token"]}")
        }
        assertEquals(HttpStatusCode.Companion.OK, deleteResponse.status) // check if the user was deleted

        val responseBody = deleteResponse.bodyAsText()
        assertTrue(responseBody.contains("Account deleted successfully"))
    }
}