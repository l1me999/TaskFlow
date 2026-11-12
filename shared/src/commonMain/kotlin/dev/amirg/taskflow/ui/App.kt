package dev.amirg.taskflow.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.model.Project
import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.model.isOverdue
import dev.amirg.taskflow.model.sortedTasks
import dev.amirg.taskflow.platform.todayIsoDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(store: TaskStore) {
    TaskFlowTheme {
        val data = store.data
        var selectedProjectId by remember { mutableStateOf<String?>(null) }
        var showEditor by remember { mutableStateOf(false) }
        val today = remember { todayIsoDate() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("TaskFlow", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${data.tasks.count { !it.done }} активных задач",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = { Text("Задача") },
                    icon = { Text("+", fontSize = 20.sp) },
                    onClick = { showEditor = true },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                ProjectRow(
                    projects = data.projects,
                    selectedProjectId = selectedProjectId,
                    onSelect = { selectedProjectId = it },
                )

                val visibleTasks = data.sortedTasks()
                    .filter { selectedProjectId == null || it.projectId == selectedProjectId }

                if (visibleTasks.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(visibleTasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                project = data.projectOf(task),
                                overdue = task.isOverdue(today),
                                onToggle = { store.toggleTask(task.id) },
                                onDelete = { store.deleteTask(task.id) },
                            )
                        }
                    }
                }
            }
        }

        if (showEditor) {
            TaskEditorDialog(
                projects = data.projects,
                defaultProjectId = selectedProjectId,
                onCreateProject = { name -> store.addProject(name).id },
                onDismiss = { showEditor = false },
                onConfirm = { title, notes, dueDate, projectId ->
                    store.addTask(
                        title = title,
                        notes = notes,
                        dueDate = dueDate,
                        projectId = projectId,
                    )
                    showEditor = false
                },
            )
        }
    }
}

@Composable
private fun ProjectRow(
    projects: List<Project>,
    selectedProjectId: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProjectChip("Все", selectedProjectId == null) { onSelect(null) }
        projects.forEach { project ->
            ProjectChip(project.name, selectedProjectId == project.id) { onSelect(project.id) }
        }
    }
}

@Composable
private fun ProjectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    project: Project?,
    overdue: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )

                val meta = listOfNotNull(project?.name, task.dueDate).joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (overdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }

                if (task.notes.isNotBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }

            TextButton(onClick = onDelete) { Text("Удалить") }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Пока пусто", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Нажмите «Задача», чтобы добавить первую запись.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
