package dev.amirg.taskflow

import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.TaskScope
import dev.amirg.taskflow.model.activeTasks
import dev.amirg.taskflow.model.completedTasks
import dev.amirg.taskflow.model.isOverdue
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
                task(id = "done", done = true, createdAt = 1000),
                task(id = "open", done = false, createdAt = 2000),
            ),
        )

        assertEquals(listOf("open"), data.activeTasks(TaskScope.All, "2026-11-12").map { it.id })
        assertEquals(listOf("done"), data.completedTasks(TaskScope.All, "2026-11-12").map { it.id })
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

        // "Планируемое" показывает только задачи со сроком, сортировка по дате
        assertEquals(
            listOf("sooner", "later"),
            data.activeTasks(TaskScope.Planned, "2026-11-12").map { it.id },
        )

        // "Все" показывает все открытые, без срока — в конце
        assertEquals(
            listOf("sooner", "later", "no-date"),
            data.activeTasks(TaskScope.All, "2026-11-12").map { it.id },
        )
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
        createdAt: Long = 0,
    ) = dev.amirg.taskflow.model.Task(
        id = id,
        title = id,
        dueDate = dueDate,
        done = done,
        createdAt = createdAt,
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
    fun projectRenameIsPersistedAndBlankNamesAreIgnored() {
        withTempFile { path ->
            val store = TaskStore(path)
            val project = store.addProject("Работа")

            store.updateProject(project.id, "  Учёба  ", "#C239B3")
            store.updateProject(project.id, "   ", "#000000")

            val saved = TaskStore(path).data.projects.single()
            assertEquals("Учёба", saved.name)
            assertEquals("#C239B3", saved.colorHex)
        }
    }

    @Test
    fun deletingAProjectKeepsItsTasksWithoutAProject() {
        withTempFile { path ->
            val store = TaskStore(path)
            val project = store.addProject("Работа")
            val task = store.addTask(title = "Отчёт", projectId = project.id)

            store.deleteProject(project.id)

            val reopened = TaskStore(path)
            assertTrue(reopened.data.projects.isEmpty())
            assertEquals(task.id, reopened.data.tasks.single().id)
            assertEquals(null, reopened.data.tasks.single().projectId)
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
