package dev.amirg.taskflow.model

import kotlinx.serialization.Serializable

/**
 * Everything the app stores. Serialized to `taskflow.json`.
 *
 * [schemaVersion] exists so a future storage migration can detect old files.
 * Version 2 added the `important` flag to tasks; older files load unchanged
 * because every new field has a default.
 */
@Serializable
data class TaskFlowData(
    val schemaVersion: Int = 2,
    val projects: List<Project> = emptyList(),
    val tasks: List<Task> = emptyList(),
) {
    fun projectOf(task: Task): Project? = projects.firstOrNull { it.id == task.projectId }
}

/** Overdue means: has a due date, the due date already passed, and not done yet. */
fun Task.isOverdue(todayIsoDate: String): Boolean =
    !done && dueDate != null && dueDate < todayIsoDate
