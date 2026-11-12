package dev.amirg.taskflow

import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.isOverdue
import dev.amirg.taskflow.model.sortedTasks
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaskOrderingTest {
    @Test
    fun openTasksComeBeforeDoneOnes() {
        val data = TaskFlowData(
            tasks = listOf(
                task(id = "done", done = true, dueDate = "2026-01-01"),
                task(id = "open", done = false, dueDate = "2026-12-31"),
            ),
        )

        assertEquals(listOf("open", "done"), data.sortedTasks().map { it.id })
    }

    @Test
    fun deadlineOrdersTasksAndUndatedGoLast() {
        val data = TaskFlowData(
            tasks = listOf(
                task(id = "no-date"),
                task(id = "later", dueDate = "2026-12-01"),
                task(id = "sooner", dueDate = "2026-11-15"),
            ),
        )

        assertEquals(listOf("sooner", "later", "no-date"), data.sortedTasks().map { it.id })
    }

    @Test
    fun overdueNeedsDeadlineAndOpenState() {
        val today = "2026-11-12"

        assertTrue(task(id = "late", dueDate = "2026-11-11").isOverdue(today))
        assertFalse(task(id = "today", dueDate = today).isOverdue(today))
        assertFalse(task(id = "future", dueDate = "2026-11-13").isOverdue(today))
        assertFalse(task(id = "no-date").isOverdue(today))
        assertFalse(task(id = "done", dueDate = "2026-11-11", done = true).isOverdue(today))
    }

    private fun task(
        id: String,
        dueDate: String? = null,
        done: Boolean = false,
    ) = dev.amirg.taskflow.model.Task(
        id = id,
        title = id,
        dueDate = dueDate,
        done = done,
    )
}

class TaskStoreTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun savedDataIsRestoredFromDisk() {
        withTempFile { path ->
            val store = TaskStore(path)
            val project = store.addProject("Работа")
            val task = store.addTask(
                title = "Написать отчёт",
                projectId = project.id,
                dueDate = "2026-12-01",
                notes = "Черновик",
            )

            val reopened = TaskStore(path)

            assertEquals(1, reopened.data.projects.size)
            assertEquals("Работа", reopened.data.projects.single().name)
            assertEquals(task.id, reopened.data.tasks.single().id)
            assertEquals("Черновик", reopened.data.tasks.single().notes)
            assertEquals("2026-12-01", reopened.data.tasks.single().dueDate)
        }
    }

    @Test
    fun toggleAndDeleteArePersisted() {
        withTempFile { path ->
            val store = TaskStore(path)
            val task = store.addTask(title = "Купить хлеб")

            store.toggleTask(task.id)
            assertTrue(TaskStore(path).data.tasks.single().done)

            store.deleteTask(task.id)
            assertTrue(TaskStore(path).data.tasks.isEmpty())
        }
    }

    @Test
    fun corruptFileIsBackedUpInsteadOfCrashing() {
        withTempFile { path ->
            FileSystem.SYSTEM.createDirectories(path.parent!!)
            FileSystem.SYSTEM.write(path) { writeUtf8("{ this is not json") }

            val store = TaskStore(path)

            assertTrue(store.data.tasks.isEmpty())
            val backup = FileSystem.SYSTEM.list(path.parent!!)
                .any { it.name.startsWith("${path.name}.corrupt-") }
            assertTrue(backup, "expected a .corrupt-* backup next to ${path.name}")
        }
    }

    private fun withTempFile(block: (okio.Path) -> Unit) {
        val dir = "build/tmp/taskflow-tests-${Random.nextInt(1_000_000)}".toPath()
        val path = "$dir/store.json".toPath()
        try {
            block(path)
        } finally {
            runCatching { FileSystem.SYSTEM.delete(dir, mustExist = false) }
        }
    }
}
