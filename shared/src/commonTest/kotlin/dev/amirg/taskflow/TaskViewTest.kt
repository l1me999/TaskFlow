package dev.amirg.taskflow

import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.TaskScope
import dev.amirg.taskflow.model.activeTasks
import dev.amirg.taskflow.ui.TaskView
import dev.amirg.taskflow.ui.defaultDueDateFor
import dev.amirg.taskflow.ui.defaultImportantFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuickAddDefaultsTest {
    private val today = "2026-11-13"

    @Test
    fun todayViewSchedulesNewTasksForToday() {
        assertEquals(today, defaultDueDateFor(TaskView.Today, today))
    }

    @Test
    fun plannedViewAlsoNeedsADate() {
        assertEquals(today, defaultDueDateFor(TaskView.Planned, today))
    }

    @Test
    fun otherViewsLeaveTheDateEmpty() {
        assertNull(defaultDueDateFor(TaskView.All, today))
        assertNull(defaultDueDateFor(TaskView.Projects, today))
        assertNull(defaultDueDateFor(TaskView.Project("p1"), today))
    }

    @Test
    fun importantViewMarksNewTasksImportant() {
        assertTrue(defaultImportantFor(TaskView.Important))
        assertFalse(defaultImportantFor(TaskView.Today))
        assertFalse(defaultImportantFor(TaskView.All))
    }

    @Test
    fun taskDatedTodayShowsUpInTheTodayList() {
        val data = TaskFlowData(
            tasks = listOf(Task(id = "t", title = "Позвонить", dueDate = today)),
        )

        assertEquals(listOf("t"), data.activeTasks(TaskScope.Today, today).map { it.id })
    }
}
