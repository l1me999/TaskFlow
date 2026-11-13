package dev.amirg.taskflow.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.amirg.taskflow.model.Project
import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.platform.nowMillis
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.random.Random

/**
 * Single source of truth for projects and tasks.
 *
 * Storage is a JSON document rewritten on every change (the data set is small and
 * personal). Writes go to a temp file first and then replace the real file, so a
 * crash never leaves a half-written document.
 *
 * Not thread-safe: call it from the UI thread only. This is fine for the MVP and
 * will be revisited when sync arrives.
 */
class TaskStore(
    private val dataFile: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    var data: TaskFlowData by mutableStateOf(load())
        private set

    fun addProject(name: String): Project {
        val project = Project(id = newId("project"), name = name.trim())
        mutate { it.copy(projects = it.projects + project) }
        return project
    }

    /**
     * Creates a task. Everything except [title] is optional on purpose: the quick
     * input only asks for a name, the rest is filled in later in the details pane.
     */
    fun addTask(
        title: String,
        projectId: String? = null,
        notes: String = "",
        dueDate: String? = null,
        important: Boolean = false,
    ): Task {
        val timestamp = nowMillis()
        val task = Task(
            id = newId("task"),
            title = title.trim(),
            projectId = projectId,
            notes = notes.trim(),
            dueDate = dueDate?.trim()?.ifBlank { null },
            important = important,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        mutate { it.copy(tasks = it.tasks + task) }
        return task
    }

    /** Replaces the editable fields of [task]. The id and timestamps are preserved. */
    fun updateTask(
        task: Task,
        title: String,
        notes: String,
        dueDate: String?,
        projectId: String?,
        important: Boolean,
    ) {
        val next = task.copy(
            title = title.trim(),
            notes = notes.trim(),
            dueDate = dueDate?.trim()?.ifBlank { null },
            projectId = projectId,
            important = important,
            updatedAt = nowMillis(),
        )
        mutate { current ->
            current.copy(tasks = current.tasks.map { if (it.id == next.id) next else it })
        }
    }

    fun toggleTask(id: String) {
        mutateTask(id) { it.copy(done = !it.done) }
    }

    fun toggleImportant(id: String) {
        mutateTask(id) { it.copy(important = !it.important) }
    }

    fun deleteTask(id: String) {
        mutate { current -> current.copy(tasks = current.tasks.filterNot { it.id == id }) }
    }

    private fun mutateTask(id: String, transform: (Task) -> Task) {
        mutate { current ->
            current.copy(
                tasks = current.tasks.map { task ->
                    if (task.id == id) transform(task).copy(updatedAt = nowMillis()) else task
                },
            )
        }
    }

    private fun mutate(transform: (TaskFlowData) -> TaskFlowData) {
        val next = transform(data)
        persist(next)
        data = next
    }

    private fun load(): TaskFlowData {
        if (!fileSystem.exists(dataFile)) return TaskFlowData()

        return try {
            val text = fileSystem.read(dataFile) { readUtf8() }
            if (text.isBlank()) TaskFlowData() else json.decodeFromString(TaskFlowData.serializer(), text)
        } catch (error: Exception) {
            // Never lose the user's file silently: keep a copy next to it and start clean.
            runCatching {
                fileSystem.copy(dataFile, "${dataFile}.corrupt-${nowMillis()}".toPath())
            }
            TaskFlowData()
        }
    }

    private fun persist(value: TaskFlowData) {
        val parent = dataFile.parent
        if (parent != null) fileSystem.createDirectories(parent)

        val tempFile = "${dataFile}.tmp".toPath()
        fileSystem.write(tempFile) {
            writeUtf8(json.encodeToString(TaskFlowData.serializer(), value))
        }

        if (fileSystem.exists(dataFile)) fileSystem.delete(dataFile)
        fileSystem.atomicMove(tempFile, dataFile)
    }

    private fun newId(prefix: String): String =
        "$prefix-${nowMillis()}-${Random.nextInt(100_000, 1_000_000)}"
}
