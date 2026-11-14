package dev.amirg.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.DatePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.amirg.taskflow.model.Project
import dev.amirg.taskflow.model.RepeatRule
import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.model.TaskFlowData

@Composable
fun TaskDetailsPane(
    modifier: Modifier = Modifier,
    task: Task,
    data: TaskFlowData,
    today: String,
    onSave: (title: String, notes: String, dueDate: String?, projectId: String?, important: Boolean, repeatRule: RepeatRule) -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onAttach: () -> Unit,
    onNewProject: () -> Unit,
) {
    TaskDetailsForm(
        modifier = modifier,
        task = task,
        projects = data.projects,
        today = today,
        onSave = onSave,
        onToggleDone = onToggleDone,
        onDelete = onDelete,
        onClose = onClose,
        onAttach = onAttach,
        onNewProject = onNewProject,
        isDialog = false,
    )
}

@Composable
fun TaskDetailsDialog(
    task: Task,
    projects: List<Project>,
    today: String,
    onSave: (title: String, notes: String, dueDate: String?, projectId: String?, important: Boolean, repeatRule: RepeatRule) -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onAttach: () -> Unit,
    onNewProject: () -> Unit,
) {
    TaskDetailsForm(
        modifier = Modifier.fillMaxSize(),
        task = task,
        projects = projects,
        today = today,
        onSave = onSave,
        onToggleDone = onToggleDone,
        onDelete = onDelete,
        onClose = onClose,
        onAttach = onAttach,
        onNewProject = onNewProject,
        isDialog = true,
    )
}

/** Shared edit form used by the desktop pane and the mobile dialog. */
@Composable
private fun TaskDetailsForm(
    modifier: Modifier,
    task: Task,
    projects: List<Project>,
    today: String,
    onSave: (title: String, notes: String, dueDate: String?, projectId: String?, important: Boolean, repeatRule: RepeatRule) -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onAttach: () -> Unit,
    onNewProject: () -> Unit,
    isDialog: Boolean,
) {
    var title by remember { mutableStateOf(task.title) }
    var notes by remember { mutableStateOf(task.notes) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var projectId by remember { mutableStateOf(task.projectId) }
    var important by remember { mutableStateOf(task.important) }
    var repeatRule by remember { mutableStateOf(task.repeatRule) }
    var dateMenuOpen by remember { mutableStateOf(false) }
    var projectMenuOpen by remember { mutableStateOf(false) }
    var repeatMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        if (isDialog) {
            DialogHeader(title = title, onClose = onClose)
        } else {
            PaneHeader(title = title)
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Title
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { /* focus stays */ }),
            )

            Spacer(Modifier.height(20.dp))

            // Done + star row
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = task.done,
                    onCheckedChange = { onToggleDone() },
                    modifier = Modifier.size(40.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                    ),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (task.done) "Завершено" else "Не завершено",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { important = !important }) {
                    TfIcon(
                        icon = if (important) TfIcon.StarFilled else TfIcon.Star,
                        size = 22.dp,
                        tint = if (important) ImportantColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Section: срок выполнения
            SectionTitle("Срок выполнения")
            DueDateChipRow(
                dueDate = dueDate,
                today = today,
                onChange = { dueDate = it },
                onMenu = { dateMenuOpen = true },
                onClear = {
                    dueDate = null
                    // Without an anchor date the rule cannot fire.
                    repeatRule = RepeatRule.None
                },
            )

            Spacer(Modifier.height(24.dp))

            // Section: повтор
            SectionTitle("Повтор")
            RepeatPickerRow(
                rule = repeatRule,
                enabled = dueDate != null,
                onMenu = { repeatMenuOpen = true },
            )

            Spacer(Modifier.height(24.dp))

            // Section: проект
            SectionTitle("Проект")
            ProjectPickerRow(
                projects = projects,
                selectedId = projectId,
                onChange = { projectId = it },
                onMenu = { projectMenuOpen = true },
                onNewProject = onNewProject,
            )

            Spacer(Modifier.height(24.dp))

            // Section: заметки
            SectionTitle("Заметки")
            TextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Добавьте заметку") },
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { /* focus stays */ }),
            )
        }

        // Bottom bar
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDelete) {
                TfIcon(
                    icon = TfIcon.Trash,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text("Отмена") }
                TextButton(
                    onClick = {
                        onSave(title, notes, dueDate, projectId, important, repeatRule)
                    },
                    enabled = title.isNotBlank(),
                ) {
                    Text("Сохранить")
                }
            }
        }

        DueDateDropdown(
            expanded = dateMenuOpen,
            today = today,
            dueDate = dueDate,
            onDismiss = { dateMenuOpen = false },
            onPick = { dueDate = it; dateMenuOpen = false },
            onCustomDate = {
                dateMenuOpen = false
                showDatePicker = true
            },
        )

        ProjectDropdown(
            expanded = projectMenuOpen,
            projects = projects,
            selectedId = projectId,
            onDismiss = { projectMenuOpen = false },
            onPick = { projectId = it },
        )

        RepeatDropdown(
            expanded = repeatMenuOpen,
            selected = repeatRule,
            onDismiss = { repeatMenuOpen = false },
            onPick = { repeatRule = it },
        )

        if (showDatePicker) {
            DatePickerSheet(
                initialIsoDate = dueDate ?: today,
                onDismiss = { showDatePicker = false },
                onPick = {
                    dueDate = it
                    showDatePicker = false
                },
            )
        }
    }
}

@Composable
private fun DialogHeader(title: String, onClose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onClose) {
            TfIcon(icon = TfIcon.Close, size = 20.dp)
        }
    }
}

@Composable
private fun PaneHeader(title: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun DueDateChipRow(
    dueDate: String?,
    today: String,
    onChange: (String?) -> Unit,
    onMenu: () -> Unit,
    onClear: () -> Unit,
) {
    if (dueDate == null) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenu) {
                TfIcon(
                    icon = TfIcon.Today,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Нет срока", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onMenu) { Text("Выбрать") }
        }
        return
    }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TfIcon(
            icon = TfIcon.Today,
            size = 20.dp,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatDueDate(dueDate, today),
            style = MaterialTheme.typography.bodyMedium,
            color = if (dueDate < today) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onMenu) { Text("Изменить") }
            TextButton(onClick = onClear) {
                Text(
                    "Убрать",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error),
                )
            }
        }
    }
}

@Composable
private fun RepeatPickerRow(
    rule: RepeatRule,
    enabled: Boolean,
    onMenu: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TfIcon(
            icon = TfIcon.Repeat,
            size = 20.dp,
            tint = if (rule != RepeatRule.None) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (enabled) rule.label() else "Сначала задайте срок",
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )
        if (enabled) {
            TextButton(onClick = onMenu) { Text("Изменить") }
        }
    }
}

@Composable
private fun RepeatDropdown(
    expanded: Boolean,
    selected: RepeatRule,
    onDismiss: () -> Unit,
    onPick: (RepeatRule) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        RepeatRule.entries.forEach { rule ->
            DropdownMenuItem(
                text = { Text(rule.label()) },
                leadingIcon = {
                    if (rule == selected) {
                        TfIcon(
                            icon = TfIcon.Check,
                            size = 16.dp,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                onClick = {
                    onPick(rule)
                    onDismiss()
                },
            )
        }
    }
}

@Composable
private fun ProjectPickerRow(
    projects: List<Project>,
    selectedId: String?,
    onChange: (String?) -> Unit,
    onMenu: () -> Unit,
    onNewProject: () -> Unit,
) {
    val selected = projects.firstOrNull { it.id == selectedId }
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(parseProjectColor(selected?.colorHex ?: "#4F6BED"), CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = selected?.name ?: "Без проекта",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onMenu) { Text("Изменить") }
        TextButton(onClick = onNewProject) { Text("Новый") }
    }
}

@Composable
private fun ProjectDropdown(
    expanded: Boolean,
    projects: List<Project>,
    selectedId: String?,
    onDismiss: () -> Unit,
    onPick: (String?) -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Без проекта") },
            onClick = { onPick(null); onDismiss() },
        )
        HorizontalDivider()
        projects.forEach { project ->
            DropdownMenuItem(
                text = { Text(project.name) },
                leadingIcon = {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(parseProjectColor(project.colorHex), CircleShape),
                    )
                },
                onClick = { onPick(project.id); onDismiss() },
            )
        }
    }
}

@Composable
private fun notesPlaceholder(notes: String): String =
    if (notes.length > 80) "${notes.substring(0, 80)}…" else notes
