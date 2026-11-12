package dev.amirg.taskflow.model

import kotlinx.serialization.Serializable

/**
 * Everything the app stores. Serialized to `taskflow.json`.
 *
 * [schemaVersion] exists so a future storage migration can detect old files.
 */
@Serializable
data class TaskFlowData(
    val schemaVersion: Int = 1,
    val projects: List<Project> = emptyList(),
    val tasks: List<Task> = emptyList(),
) {
    fun projectOf(task: Task): Project? = projects.firstOrNull { it.id == task.projectId }
}

/**
 * Open tasks first, then the nearest deadline, then the most recently created.
 * Tasks without a deadline go after dated ones.
 */
fun TaskFlowData.sortedTasks(): List<Task> = tasks.sortedWith(
    compareBy<Task> { it.done }
        .thenBy { it.dueDate == null }
        .thenBy { it.dueDate ?: "" }
        .thenByDescending { it.createdAt },
)

/** Overdue means: has a deadline, the deadline already passed, and not done yet. */
fun Task.isOverdue(todayIsoDate: String): Boolean =
    !done && dueDate != null && dueDate < todayIsoDate
