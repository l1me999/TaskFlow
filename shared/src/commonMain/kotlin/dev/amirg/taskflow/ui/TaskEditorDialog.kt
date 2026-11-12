package dev.amirg.taskflow.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.amirg.taskflow.model.Project

private val IsoDatePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")

@Composable
fun TaskEditorDialog(
    projects: List<Project>,
    defaultProjectId: String?,
    onCreateProject: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, notes: String, dueDate: String?, projectId: String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var projectId by remember { mutableStateOf(defaultProjectId) }
    var showProjectField by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }

    val dueDateValid = dueDate.isBlank() || IsoDatePattern.matches(dueDate)
    val canSave = title.isNotBlank() && dueDateValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Заметки") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Дедлайн") },
                    placeholder = { Text("2026-11-30") },
                    singleLine = true,
                    isError = !dueDateValid,
                    supportingText = {
                        if (!dueDateValid) Text("Формат: ГГГГ-ММ-ДД")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text("Проект", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProjectChoice("Без проекта", projectId == null) { projectId = null }
                    projects.forEach { project ->
                        ProjectChoice(project.name, projectId == project.id) { projectId = project.id }
                    }
                    ProjectChoice("+ проект", showProjectField) { showProjectField = !showProjectField }
                }

                if (showProjectField) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = newProjectName,
                            onValueChange = { newProjectName = it },
                            label = { Text("Название проекта") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                projectId = onCreateProject(newProjectName)
                                newProjectName = ""
                                showProjectField = false
                            },
                            enabled = newProjectName.isNotBlank(),
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text("Добавить")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        title.trim(),
                        notes.trim(),
                        dueDate.ifBlank { null },
                        projectId,
                    )
                },
                enabled = canSave,
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    )
}

@Composable
private fun ProjectChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}
