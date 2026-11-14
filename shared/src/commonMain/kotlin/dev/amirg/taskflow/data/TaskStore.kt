package dev.amirg.taskflow.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.amirg.taskflow.model.Project
import dev.amirg.taskflow.model.RepeatRule
import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.nextOccurrence
import dev.amirg.taskflow.platform.nowMillis
import dev.amirg.taskflow.platform.todayIsoDate
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

    /** Renames [id] and recolors it. A blank name is ignored: a project always has one. */
    fun updateProject(id: String, name: String, colorHex: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        mutate { current ->
            current.copy(
                projects = current.projects.map { project ->
                    if (project.id == id) project.copy(name = trimmed, colorHex = colorHex) else project
                },
            )
        }
    }

    /**
     * Removes a project without touching its tasks: they stay in "Все задачи"
     * with no project. Deleting work the user wrote is never a side effect of
     * deleting a folder-like object.
     */
    fun deleteProject(id: String) {
        mutate { current ->
            current.copy(
                projects = current.projects.filterNot { it.id == id },
                tasks = current.tasks.map { task ->
                    if (task.projectId == id) task.copy(projectId = null, updatedAt = nowMillis()) else task
                },
            )
        }
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
        repeatRule: RepeatRule = RepeatRule.None,
    ): Task {
        val timestamp = nowMillis()
        val task = Task(
            id = newId("task"),
            title = title.trim(),
            projectId = projectId,
            notes = notes.trim(),
            dueDate = dueDate?.trim()?.ifBlank { null },
            important = important,
            repeatRule = repeatRule,
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
        repeatRule: RepeatRule,
    ) {
        // A repeat rule without a due date has no anchor, so it is dropped
        // instead of silently doing nothing later.
        val cleanDueDate = dueDate?.trim()?.ifBlank { null }
        val next = task.copy(
            title = title.trim(),
            notes = notes.trim(),
            dueDate = cleanDueDate,
            projectId = projectId,
            important = important,
            repeatRule = if (cleanDueDate == null) RepeatRule.None else repeatRule,
            updatedAt = nowMillis(),
        )
        mutate { current ->
            current.copy(tasks = current.tasks.map { if (it.id == next.id) next else it })
        }
    }

    /**
     * Toggles `done`. Completing a recurring task also closes the current
     * occurrence and adds the next one with the same text, project and star,
     * so the completed entry stays as history.
     */
    fun toggleTask(id: String, today: String = todayIsoDate()) {
        mutate { current ->
            val task = current.tasks.firstOrNull { it.id == id } ?: return@mutate current
            val timestamp = nowMillis()

            if (!task.done && task.repeatRule != RepeatRule.None) {
                val nextDate = task.repeatRule.nextOccurrence(task.dueDate, today)
                val closed = task.copy(done = true, updatedAt = timestamp)
                val next = nextDate?.let { date ->
                    Task(
                        id = newId("task"),
                        title = task.title,
                        projectId = task.projectId,
                        notes = task.notes,
                        dueDate = date,
                        important = task.important,
                        repeatRule = task.repeatRule,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    )
                }
                current.copy(
                    tasks = current.tasks.map { if (it.id == id) closed else it } +
                        listOfNotNull(next),
                )
            } else {
                current.copy(
                    tasks = current.tasks.map {
                        if (it.id == id) task.copy(done = !task.done, updatedAt = timestamp) else it
                    },
                )
            }
        }
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
