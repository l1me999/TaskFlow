package dev.amirg.taskflow.model

/**
 * The lists shown in the navigation panel.
 *
 * [Project] is a scope narrowed to a single project id passed alongside it.
 */
enum class TaskScope {
    Today,
    Important,
    Planned,
    All,
    Project,
}

/**
 * Open tasks for [scope], in display order.
 *
 * "Сегодня" includes overdue tasks, matching how Microsoft To Do behaves: the
 * list is what needs attention today, not only tasks dated exactly today.
 */
fun TaskFlowData.activeTasks(
    scope: TaskScope,
    today: String,
    projectId: String? = null,
): List<Task> = tasks
    .asSequence()
    .filter { !it.done && it.matches(scope, today, projectId) }
    .sortedWith(activeOrder(scope))
    .toList()

/**
 * Completed tasks belonging to [scope], newest first. "Сегодня" only shows
 * tasks completed for today's date, so the section does not fill up forever.
 */
fun TaskFlowData.completedTasks(
    scope: TaskScope,
    today: String,
    projectId: String? = null,
): List<Task> = tasks
    .asSequence()
    .filter { it.done && it.matches(scope, today, projectId) }
    .sortedByDescending { it.updatedAt }
    .toList()

/** How many open tasks each scope holds — used for the sidebar badges. */
fun TaskFlowData.openCount(scope: TaskScope, today: String, projectId: String? = null): Int =
    tasks.count { !it.done && it.matches(scope, today, projectId) }

fun TaskFlowData.openCountForProject(projectId: String): Int =
    tasks.count { !it.done && it.projectId == projectId }

private fun Task.matches(scope: TaskScope, today: String, projectId: String?): Boolean = when (scope) {
    TaskScope.All -> true
    TaskScope.Today -> dueDate != null && dueDate <= today
    TaskScope.Important -> important
    TaskScope.Planned -> dueDate != null
    TaskScope.Project -> this.projectId == projectId
}

private fun activeOrder(scope: TaskScope): Comparator<Task> = when (scope) {
    // Date-driven lists: nearest date first, with important tasks winning ties.
    TaskScope.Today, TaskScope.Planned ->
        compareBy<Task> { it.dueDate ?: "" }
            .thenByDescending { it.important }
            .thenByDescending { it.createdAt }

    // Mixed lists: starred first, then dated tasks, then the rest.
    TaskScope.All, TaskScope.Important, TaskScope.Project ->
        compareByDescending<Task> { it.important }
            .thenBy { it.dueDate == null }
            .thenBy { it.dueDate ?: "" }
            .thenByDescending { it.createdAt }
}
