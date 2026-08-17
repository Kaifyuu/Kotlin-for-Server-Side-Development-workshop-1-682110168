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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Workshop5Test {

    // --- Repository / business-logic level tests ---

    @BeforeTest
    fun setup() {
        PollRepository.connect()
    }

    @Test
    fun `createOption returns null when poll does not exist`() {
        val result = PollRepository.createOption(pollId = 999, text = "Blue")

        assertEquals(null, result)
    }

    @Test
    fun `vote increments voteCount by 1 without race on repeated calls`() {
        val poll = PollRepository.createPoll("Favorite color?")
        val option = PollRepository.createOption(poll.id, "Blue")!!

        PollRepository.vote(option.id)
        PollRepository.vote(option.id)
        val result = PollRepository.vote(option.id)

        assertEquals(3, result?.voteCount)
    }

    @Test
    fun `vote returns null when option does not exist`() {
        val result = PollRepository.vote(999)

        assertEquals(null, result)
    }

    @Test
    fun `getPollWithOptions joins poll and its options`() {
        val poll = PollRepository.createPoll("Favorite language?")
        PollRepository.createOption(poll.id, "Kotlin")
        PollRepository.createOption(poll.id, "Java")

        val result = PollRepository.getPollWithOptions(poll.id)!!

        assertEquals("Favorite language?", result.question)
        assertEquals(2, result.options.size)
    }

    @Test
    fun `deletePoll cascades to its options`() {
        val poll = PollRepository.createPoll("To be deleted")
        val option = PollRepository.createOption(poll.id, "Option A")!!

        val deleted = PollRepository.deletePoll(poll.id)

        assertTrue(deleted)
        assertEquals(null, PollRepository.getOptionById(option.id))
    }

    // --- HTTP-level tests ---

    @Test
    fun `POST polls creates a poll and returns 201`() = testApplication {
        application { pollModule() }
        val client = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }

        val response = client.post("/polls") {
            contentType(ContentType.Application.Json)
            setBody(CreatePollRequest(question = "Best framework?"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `GET polls by id returns 404 when not found`() = testApplication {
        application { pollModule() }

        val response = client.get("/polls/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST options vote returns updated option with 200`() = testApplication {
        application { pollModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }
        val poll: PollDto = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/polls") {
                contentType(ContentType.Application.Json)
                setBody(CreatePollRequest(question = "Vote test?"))
            }.bodyAsText()
        )
        val option: PollOptionDto = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/polls/${poll.id}/options") {
                contentType(ContentType.Application.Json)
                setBody(CreatePollOptionRequest(text = "Yes"))
            }.bodyAsText()
        )

        val response = client.post("/options/${option.id}/vote")

        assertEquals(HttpStatusCode.OK, response.status)
        val voted: PollOptionDto = kotlinx.serialization.json.Json.decodeFromString(response.bodyAsText())
        assertEquals(1, voted.voteCount)
    }

    @Test
    fun `POST options vote returns 404 when option does not exist`() = testApplication {
        application { pollModule() }

        val response = client.post("/options/999/vote")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `DELETE polls returns 204 and cascades, subsequent GET options is 404`() = testApplication {
        application { pollModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }
        val poll: PollDto = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/polls") {
                contentType(ContentType.Application.Json)
                setBody(CreatePollRequest(question = "Delete cascade test?"))
            }.bodyAsText()
        )
        val option: PollOptionDto = kotlinx.serialization.json.Json.decodeFromString(
            jsonClient.post("/polls/${poll.id}/options") {
                contentType(ContentType.Application.Json)
                setBody(CreatePollOptionRequest(text = "A"))
            }.bodyAsText()
        )

        val deleteResponse = client.delete("/polls/${poll.id}")
        val followupResponse = client.get("/options/${option.id}")

        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)
        assertEquals(HttpStatusCode.NotFound, followupResponse.status)
    }

    @Test
    fun `PUT polls returns 404 when poll does not exist`() = testApplication {
        application { pollModule() }
        val jsonClient = createClient { install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { json() } }

        val response = jsonClient.put("/polls/999") {
            contentType(ContentType.Application.Json)
            setBody(CreatePollRequest(question = "Nope"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
