package org.example

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Workshop4Test {

    @AfterTest
    fun cleanup() {
        TaskRepository.reset()
    }

    @Test
    fun `GET tasks returns empty list when repository is empty`() = testApplication {
        application { taskModule() }

        val response = client.get("/tasks")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText())
    }

    @Test
    fun `POST tasks creates a task and returns 201`() = testApplication {
        application { taskModule() }
        val client = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }

        val response = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequest(content = "Buy milk", isDone = false))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val created: Task = kotlinx.serialization.json.Json.decodeFromString(response.bodyAsText())
        assertEquals("Buy milk", created.content)
        assertTrue(created.id > 0)
    }

    @Test
    fun `GET tasks by id returns 404 when not found`() = testApplication {
        application { taskModule() }

        val response = client.get("/tasks/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET tasks by id returns 400 when id is not an integer`() = testApplication {
        application { taskModule() }

        val response = client.get("/tasks/abc")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET tasks by id returns the task when found`() = testApplication {
        application { taskModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }
        val created: Task = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(TaskRequest(content = "Read book", isDone = false))
            }.bodyAsText()
        )

        val response = client.get("/tasks/${created.id}")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT tasks updates an existing task`() = testApplication {
        application { taskModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }
        val created: Task = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(TaskRequest(content = "Draft", isDone = false))
            }.bodyAsText()
        )

        val response = jsonClient.put("/tasks/${created.id}") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequest(content = "Final", isDone = true))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val updated: Task = kotlinx.serialization.json.Json.decodeFromString(response.bodyAsText())
        assertEquals("Final", updated.content)
        assertTrue(updated.isDone)
    }

    @Test
    fun `PUT tasks returns 404 when task does not exist`() = testApplication {
        application { taskModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }

        val response = jsonClient.put("/tasks/999") {
            contentType(ContentType.Application.Json)
            setBody(TaskRequest(content = "Nope", isDone = false))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE tasks removes an existing task and returns 204`() = testApplication {
        application { taskModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }
        val created: Task = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/tasks") {
                contentType(ContentType.Application.Json)
                setBody(TaskRequest(content = "Delete me", isDone = false))
            }.bodyAsText()
        )

        val response = client.delete("/tasks/${created.id}")

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `DELETE tasks returns 404 when task does not exist`() = testApplication {
        application { taskModule() }

        val response = client.delete("/tasks/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
