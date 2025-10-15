package nl.connectplay.scoreplay.tests

import nl.connectplay.scoreplay.module
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.* // provides Ktor’s built-in test engine
import kotlin.test.* // provides Kotlin test functions (assert, @Test, etc.)
import io.ktor.client.statement.bodyAsText

class UserRoutesTests {
    @Test // marks this function as a test
    fun testRegisterUser() {
        testApplication { // runs the Ktor test environment
            application { module() } // boot the real app: JSON + routes via annotation scanner

            val firstResponse = client.post("/register") { // send a POST request to the /register endpoint
                setBody("""{"id":"1","username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""") // the is what the receiver wants call.receive<Map<String, String>>()
                header(
                HttpHeaders.ContentType,
                ContentType.Application.Json) // tells the server where sending Json data
            }
            assertEquals(HttpStatusCode.Created, firstResponse.status)

            // check if user exist
            val secondResponse = client.post("/register") {
                setBody("""{"id":"2","username":"Mario","email":"mario@connect-play.nl, "password":"Welkom01"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            assertEquals(HttpStatusCode.Conflict, secondResponse.status)
        }
    }

    @Test
    fun testUpdateUser() {
        testApplication {
            application { module() }
            client.post("/register")  {
                setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            val patchResponse = client.patch("/users/1"){
                setBody("""{"username":"Luigi","email":"luigi@connect-play.nl"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            // check if the update was successful
            assertEquals(HttpStatusCode.OK, patchResponse.status)

            // check if the response data match
            val responseBody = patchResponse.bodyAsText()
            assertTrue(responseBody.contains("Luigi"))
            assertTrue(responseBody.contains("luigi@connect-play.nl"))
        }
    }

    @Test
    fun testDeleteUser() {
        testApplication {
            application { module() }
            client.post("/register")  {
                setBody("""{"username":"Mario","email":"mario@connect-play.nl", "password":"Welkom01"}""")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            val deleteResponse = client.delete("/users/1")
            assertEquals(HttpStatusCode.OK, deleteResponse.status) // check if the user was deleted

            val responseBody = deleteResponse.bodyAsText()
            assertTrue(responseBody.contains("Account deleted successfully"))
        }
    }


}