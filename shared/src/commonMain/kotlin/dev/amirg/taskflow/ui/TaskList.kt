package dev.amirg.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.activeTasks
import dev.amirg.taskflow.model.completedTasks
import dev.amirg.taskflow.model.isOverdue

@Composable
fun TaskListContent(
    modifier: Modifier = Modifier,
    data: TaskFlowData,
    view: TaskView,
    today: String,
    onOpen: (String) -> Unit,
    onToggle: (String) -> Unit,
    onToggleImportant: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val projectId = (view as? TaskView.Project)?.id
    val active = data.activeTasks(view.scope, today, projectId)
    val completed = data.completedTasks(view.scope, today, projectId)
    var showCompleted by remember(view) { mutableStateOf(false) }

    if (active.isEmpty() && completed.isEmpty()) {
        EmptyState(view, modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 6.dp),
    ) {
        items(active, key = { it.id }) { task ->
            TaskRow(
                task = task,
                project = data.projectOf(task),
                today = today,
                onOpen = { onOpen(task.id) },
                onToggle = { onToggle(task.id) },
                onToggleImportant = { onToggleImportant(task.id) },
                onDelete = { onDelete(task.id) },
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(start = 64.dp, end = 16.dp),
            )
        }

        if (completed.isNotEmpty()) {
            item(key = "completed-header") {
                CompletedHeader(
                    count = completed.size,
                    expanded = showCompleted,
                    onToggle = { showCompleted = !showCompleted },
                )
            }
            if (showCompleted) {
                items(completed, key = { "done-${it.id}" }) { task ->
                    TaskRow(
                        task = task,
                        project = data.projectOf(task),
                        today = today,
                        onOpen = { onOpen(task.id) },
                        onToggle = { onToggle(task.id) },
                        onToggleImportant = { onToggleImportant(task.id) },
                        onDelete = { onDelete(task.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    project: dev.amirg.taskflow.model.Project?,
    today: String,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onToggleImportant: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val overdue = task.isOverdue(today)

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .heightIn(min = 58.dp)
            .padding(start = 10.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = task.done,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(44.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(Modifier.width(6.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
                color = if (task.done) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            val dateText = task.dueDate?.let { formatDueDate(it, today) }
            val dateColor = when {
                task.done -> MaterialTheme.colorScheme.onSurfaceVariant
                overdue -> MaterialTheme.colorScheme.error
                task.dueDate == today -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            if (project != null || dateText != null) {
                Row(
                    Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (project != null) {
                        Box(
                            Modifier
                                .size(7.dp)
                                .background(parseProjectColor(project.colorHex), CircleShape),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (project != null && dateText != null) {
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Only the date turns red when overdue: the project name stays
                    // neutral, otherwise the whole row looks like an error.
                    if (dateText != null) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = dateColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        IconButton(onClick = onToggleImportant) {
            TfIcon(
                icon = if (task.important) TfIcon.StarFilled else TfIcon.Star,
                size = 22.dp,
                tint = if (task.important) ImportantColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box {
            TextButton(onClick = { menuOpen = true }) {
                TfIcon(
                    icon = TfIcon.More,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Открыть") },
                    leadingIcon = { TfIcon(TfIcon.Edit, size = 18.dp) },
                    onClick = {
                        menuOpen = false
                        onOpen()
                    },
                )
                DropdownMenuItem(
                    text = { Text(if (task.important) "Убрать из важных" else "Важно") },
                    leadingIcon = { TfIcon(TfIcon.Star, size = 18.dp) },
                    onClick = {
                        menuOpen = false
                        onToggleImportant()
                    },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        TfIcon(TfIcon.Trash, size = 18.dp, tint = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun CompletedHeader(count: Int, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TfIcon(
            icon = if (expanded) TfIcon.Expand else TfIcon.Collapse,
            size = 16.dp,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Выполненные",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(view: TaskView, modifier: Modifier = Modifier) {
    val (icon, title, hint) = when (view) {
        TaskView.Today -> Triple(TfIcon.Today, "На сегодня всё", "Задач со сроком на сегодня нет.")
        TaskView.Important -> Triple(TfIcon.Star, "Нет важных задач", "Отметьте задачу звёздочкой, чтобы она появилась здесь.")
        TaskView.Planned -> Triple(TfIcon.Planned, "Нет задач со сроком", "Задайте срок выполнения, и задача попадёт сюда.")
        TaskView.All -> Triple(TfIcon.All, "Пока пусто", "Добавьте первую задачу в строке ниже.")
        is TaskView.Project -> Triple(TfIcon.Folder, "В проекте пусто", "Добавьте задачу в строке ниже.")
        TaskView.Projects -> Triple(TfIcon.Folder, "Проектов пока нет", "Создайте проект, чтобы группировать задачи.")
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TfIcon(
            icon = icon,
            size = 48.dp,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
