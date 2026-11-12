package dev.amirg.taskflow.model

import kotlinx.serialization.Serializable

/**
 * A user-defined grouping for tasks (a project, a course, a side hustle).
 */
@Serializable
data class Project(
    val id: String,
    val name: String,
    val colorHex: String = "#4F6BED",
)

/**
 * A single task.
 *
 * [dueDate] is an ISO-8601 calendar date (`YYYY-MM-DD`) or `null` when the task
 * has no deadline. ISO dates sort lexicographically, so plain string comparison
 * is enough for ordering and "overdue" checks.
 */
@Serializable
data class Task(
    val id: String,
    val title: String,
    val projectId: String? = null,
    val notes: String = "",
    val dueDate: String? = null,
    val done: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
