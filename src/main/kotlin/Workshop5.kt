package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

// --- Tables ---

object Polls : Table("polls") {
    val id = integer("id").autoIncrement()
    val question = varchar("question", 500)
    override val primaryKey = PrimaryKey(id)
}

object PollOptions : Table("poll_options") {
    val id = integer("id").autoIncrement()
    val text = varchar("text", 500)
    val voteCount = integer("vote_count").default(0)
    val pollId = integer("poll_id").references(Polls.id)
    override val primaryKey = PrimaryKey(id)
}

// --- DTOs ---

@Serializable
data class PollDto(val id: Int, val question: String)

@Serializable
data class PollOptionDto(val id: Int, val text: String, val voteCount: Int, val pollId: Int)

@Serializable
data class PollWithOptionsDto(val id: Int, val question: String, val options: List<PollOptionDto>)

@Serializable
data class CreatePollRequest(val question: String)

@Serializable
data class CreatePollOptionRequest(val text: String)

// --- Repository ---

object PollRepository {

    fun connect() {
        Database.connect("jdbc:h2:mem:polldb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.drop(PollOptions, Polls)
            SchemaUtils.create(Polls, PollOptions)
        }
    }

    private fun toPollDto(row: ResultRow) = PollDto(id = row[Polls.id], question = row[Polls.question])

    private fun toOptionDto(row: ResultRow) = PollOptionDto(
        id = row[PollOptions.id],
        text = row[PollOptions.text],
        voteCount = row[PollOptions.voteCount],
        pollId = row[PollOptions.pollId]
    )

    fun getAllPolls(): List<PollDto> = transaction {
        Polls.selectAll().map(::toPollDto)
    }

    fun getPollById(id: Int): PollDto? = transaction {
        Polls.selectAll().where { Polls.id eq id }.map(::toPollDto).firstOrNull()
    }

    fun getPollWithOptions(id: Int): PollWithOptionsDto? = transaction {
        val poll = Polls.selectAll().where { Polls.id eq id }.map(::toPollDto).firstOrNull() ?: return@transaction null
        val options = PollOptions.selectAll().where { PollOptions.pollId eq id }.map(::toOptionDto)
        PollWithOptionsDto(id = poll.id, question = poll.question, options = options)
    }

    fun createPoll(question: String): PollDto = transaction {
        val newId = Polls.insert { it[Polls.question] = question } get Polls.id
        PollDto(id = newId, question = question)
    }

    fun updatePoll(id: Int, question: String): PollDto? = transaction {
        val updatedRows = Polls.update({ Polls.id eq id }) { it[Polls.question] = question }
        if (updatedRows == 0) null else PollDto(id = id, question = question)
    }

    fun deletePoll(id: Int): Boolean = transaction {
        val poll = Polls.selectAll().where { Polls.id eq id }.firstOrNull() ?: return@transaction false
        PollOptions.deleteWhere { with(SqlExpressionBuilder) { PollOptions.pollId eq id } }
        Polls.deleteWhere { with(SqlExpressionBuilder) { Polls.id eq id } }
        true
    }

    fun getOptionById(id: Int): PollOptionDto? = transaction {
        PollOptions.selectAll().where { PollOptions.id eq id }.map(::toOptionDto).firstOrNull()
    }

    fun createOption(pollId: Int, text: String): PollOptionDto? = transaction {
        val pollExists = Polls.selectAll().where { Polls.id eq pollId }.any()
        if (!pollExists) return@transaction null
        val newId = PollOptions.insert {
            it[PollOptions.text] = text
            it[PollOptions.pollId] = pollId
        } get PollOptions.id
        PollOptionDto(id = newId, text = text, voteCount = 0, pollId = pollId)
    }

    fun updateOption(id: Int, text: String): PollOptionDto? = transaction {
        val updatedRows = PollOptions.update({ PollOptions.id eq id }) { it[PollOptions.text] = text }
        if (updatedRows == 0) return@transaction null
        PollOptions.selectAll().where { PollOptions.id eq id }.map(::toOptionDto).firstOrNull()
    }

    fun deleteOption(id: Int): Boolean = transaction {
        PollOptions.deleteWhere { with(SqlExpressionBuilder) { PollOptions.id eq id } } > 0
    }

    // Column-reference update: increments in SQL, not via read-then-write in Kotlin
    fun vote(id: Int): PollOptionDto? = transaction {
        val updatedRows = PollOptions.update({ PollOptions.id eq id }) {
            it[voteCount] = with(SqlExpressionBuilder) { PollOptions.voteCount + 1 }
        }
        if (updatedRows == 0) return@transaction null
        PollOptions.selectAll().where { PollOptions.id eq id }.map(::toOptionDto).firstOrNull()
    }
}

// --- Routes ---

fun Application.pollModule() {
    PollRepository.connect()

    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/polls") {
            call.respond(HttpStatusCode.OK, PollRepository.getAllPolls())
        }

        post("/polls") {
            val request = call.receive<CreatePollRequest>()
            val created = PollRepository.createPoll(request.question)
            call.respond(HttpStatusCode.Created, created)
        }

        get("/polls/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val poll = PollRepository.getPollWithOptions(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "poll $id not found")
            call.respond(HttpStatusCode.OK, poll)
        }

        put("/polls/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val request = call.receive<CreatePollRequest>()
            val updated = PollRepository.updatePoll(id, request.question)
                ?: return@put call.respond(HttpStatusCode.NotFound, "poll $id not found")
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/polls/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            if (!PollRepository.deletePoll(id)) {
                return@delete call.respond(HttpStatusCode.NotFound, "poll $id not found")
            }
            call.respond(HttpStatusCode.NoContent)
        }

        post("/polls/{id}/options") {
            val pollId = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val request = call.receive<CreatePollOptionRequest>()
            val created = PollRepository.createOption(pollId, request.text)
                ?: return@post call.respond(HttpStatusCode.NotFound, "poll $pollId not found")
            call.respond(HttpStatusCode.Created, created)
        }

        get("/options/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val option = PollRepository.getOptionById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "option $id not found")
            call.respond(HttpStatusCode.OK, option)
        }

        put("/options/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val request = call.receive<CreatePollOptionRequest>()
            val updated = PollRepository.updateOption(id, request.text)
                ?: return@put call.respond(HttpStatusCode.NotFound, "option $id not found")
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/options/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            if (!PollRepository.deleteOption(id)) {
                return@delete call.respond(HttpStatusCode.NotFound, "option $id not found")
            }
            call.respond(HttpStatusCode.NoContent)
        }

        post("/options/{id}/vote") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val updated = PollRepository.vote(id)
                ?: return@post call.respond(HttpStatusCode.NotFound, "option $id not found")
            call.respond(HttpStatusCode.OK, updated)
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8081, module = Application::pollModule).start(wait = true)
}
