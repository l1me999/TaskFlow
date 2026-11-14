package dev.amirg.taskflow.ui

import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.TaskScope
import dev.amirg.taskflow.model.activeTasks
import dev.amirg.taskflow.model.openCount

/** What the user is looking at right now. */
sealed interface TaskView {
    val scope: TaskScope

    data object Today : TaskView {
        override val scope = TaskScope.Today
    }

    data object Important : TaskView {
        override val scope = TaskScope.Important
    }

    data object Planned : TaskView {
        override val scope = TaskScope.Planned
    }

    data object All : TaskView {
        override val scope = TaskScope.All
    }

    data class Project(val id: String) : TaskView {
        override val scope = TaskScope.Project
    }

    /** The list of projects itself — only reachable from the phone's bottom bar. */
    data object Projects : TaskView {
        override val scope = TaskScope.All
    }
}

fun TaskView.title(data: TaskFlowData): String = when (this) {
    TaskView.Today -> "Сегодня"
    TaskView.Important -> "Важно"
    TaskView.Planned -> "Планируемое"
    TaskView.All -> "Все задачи"
    is TaskView.Project -> data.projects.firstOrNull { it.id == id }?.name ?: "Проект"
    TaskView.Projects -> "Проекты"
}

fun TaskView.subtitle(data: TaskFlowData, today: String): String = when (this) {
    TaskView.Projects -> {
        val count = data.projects.size
        "$count ${pluralRu(count, "проект", "проекта", "проектов")}"
    }

    is TaskView.Project -> {
        val count = data.activeTasks(scope, today, id).size
        "$count ${pluralRu(count, "задача", "задачи", "задач")}"
    }

    else -> {
        val count = data.openCount(scope, today)
        "$count ${pluralRu(count, "задача", "задачи", "задач")}"
    }
}

/**
 * A task added from a smart list should land in that list, otherwise it looks
 * like the quick input silently ate it. "Сегодня" and "Планируемое" therefore
 * default to today's date.
 */
internal fun defaultDueDateFor(view: TaskView, today: String): String? = when (view) {
    TaskView.Today, TaskView.Planned -> today
    TaskView.Important, TaskView.All, TaskView.Projects, is TaskView.Project -> null
}

/** Tasks added from "Важно" are important by default, matching Microsoft To Do. */
internal fun defaultImportantFor(view: TaskView): Boolean = view == TaskView.Important
