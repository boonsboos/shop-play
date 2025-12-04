package nl.connectplay.scoreplay.tests.integration

import com.auth0.jwt.JWT
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import nl.connectplay.scoreplay.models.dto.user.FullUserDto
import nl.connectplay.scoreplay.module
import org.junit.jupiter.api.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserRoutesTests {
    @Test // marks this function as a test
    // specify the order since JUnit tries to discover and run tests in parallel
    // for these specific scenarios, the tests need to run in order
    @Order(1)
    fun testRegisterUserReservesUsernameAndEmail() = testApplication { // runs the Ktor test environment
        // pass the configuration file into the test application's environment
        // the JWT plugin requires some options
        // application.yaml is in src/test/resources/application.yaml
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
        // this call should fail because a user cannot register with the same details
        val secondResponse = client.post("/register") {
            setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        // ASSERT (2)
        assertEquals(HttpStatusCode.Conflict, secondResponse.status, "a user's username and email are unique")
    }

    @Test
    @Order(2)
    fun testLoginAndUpdateUser() = testApplication {
        environment { configure("application.yaml") }
        application { module() }

        // apply JSON content negotiation plugin to check that
        // responses are the same JSON objects that the server responds with
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

        // check the login response contains valid JSON and has the token
        val jwtToken = loginResponse.body<Map<String, String>>()["token"]
            ?: fail("Token is missing")

        // retrieve the user ID from the JWT claims.
        val userId = JWT.decode(jwtToken).claims["userId"]

        // ACT
        val patchResponse = client.patch("/users/$userId") {
            setBody("""{"username":"Luigi","email":"luigi@connect-play.nl"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $jwtToken")
        }

        // ASSERT
        // check if the update was successful
        assertEquals(HttpStatusCode.OK, patchResponse.status)

        // parse the response to a UserDto
        // check if the response data match
        val responseBody = patchResponse.body<FullUserDto>()
        assertEquals("Luigi", responseBody.username)
        assertEquals("luigi@connect-play.nl", responseBody.email)
    }

    @Test
    @Order(3)
    fun testLoginAndDeleteUser() = testApplication {
        environment { configure("application.yaml") }
        application { module() }

        client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        // ARRANGE
        val loginResponse = client.post("/login") {
            // our user has changed their username
            setBody("""{"username": "Luigi","password": "Welkom01"}""")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }

        val jwtToken = loginResponse.body<Map<String, String>>()["token"]
            ?: fail("Token is missing")

        val userId = JWT.decode(jwtToken).claims["userId"]

        // ACT
        val deleteResponse = client.delete("/users/$userId") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $jwtToken")
        }

        // ASSERT
        assertEquals(HttpStatusCode.OK, deleteResponse.status) // check if the user was deleted
        val responseBody = deleteResponse.bodyAsText()
        assertTrue(responseBody.contains("Account deleted successfully"))
    }
}