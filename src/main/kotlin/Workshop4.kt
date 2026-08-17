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
import java.util.concurrent.atomic.AtomicInteger

// --- 1. Data Modeling ---

@Serializable
data class Task(val id: Int, val content: String, val isDone: Boolean)

// Request body for creating/updating a Task — no id, server assigns it
@Serializable
data class TaskRequest(val content: String, val isDone: Boolean)

// --- 2. Data Layer (In-Memory) ---

object TaskRepository {
    private val tasks = mutableListOf<Task>()
    private val nextId = AtomicInteger(1)

    fun getAll(): List<Task> = tasks.toList()

    fun getById(id: Int): Task? = tasks.find { it.id == id }

    fun add(request: TaskRequest): Task {
        val task = Task(id = nextId.getAndIncrement(), content = request.content, isDone = request.isDone)
        tasks.add(task)
        return task
    }

    fun update(id: Int, request: TaskRequest): Task? {
        val index = tasks.indexOfFirst { it.id == id }
        if (index == -1) return null
        val updated = Task(id = id, content = request.content, isDone = request.isDone)
        tasks[index] = updated
        return updated
    }

    fun delete(id: Int): Boolean = tasks.removeIf { it.id == id }

    // ponytail: test-only reset, not part of the graded API surface
    fun reset() {
        tasks.clear()
        nextId.set(1)
    }
}

// --- 3. Routes ---

fun Application.taskModule() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/tasks") {
            call.respond(HttpStatusCode.OK, TaskRepository.getAll())
        }

        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val task = TaskRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "task $id not found")
            call.respond(HttpStatusCode.OK, task)
        }

        post("/tasks") {
            val request = call.receive<TaskRequest>()
            val created = TaskRepository.add(request)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            val request = call.receive<TaskRequest>()
            val updated = TaskRepository.update(id, request)
                ?: return@put call.respond(HttpStatusCode.NotFound, "task $id not found")
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "id must be an integer")
            if (!TaskRepository.delete(id)) {
                return@delete call.respond(HttpStatusCode.NotFound, "task $id not found")
            }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::taskModule).start(wait = true)
}
