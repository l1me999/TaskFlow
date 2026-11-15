package dev.amirg.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
    // Keyed by task id: opening another task must not show the previous one's text.
    val taskId = task.id
    var title by remember(taskId) { mutableStateOf(task.title) }
    var notes by remember(taskId) { mutableStateOf(task.notes) }
    var dueDate by remember(taskId) { mutableStateOf(task.dueDate) }
    var projectId by remember(taskId) { mutableStateOf(task.projectId) }
    var important by remember(taskId) { mutableStateOf(task.important) }
    var repeatRule by remember(taskId) { mutableStateOf(task.repeatRule) }
    var dateMenuOpen by remember(taskId) { mutableStateOf(false) }
    var projectMenuOpen by remember(taskId) { mutableStateOf(false) }
    var repeatMenuOpen by remember(taskId) { mutableStateOf(false) }
    var showDatePicker by remember(taskId) { mutableStateOf(false) }

    // Edits are local until the panel closes; closing through the cross, Esc or
    // "Готово" saves them, so there is no separate "Отмена" that could silently
    // throw away typing. A blank title keeps the old one instead of failing.
    fun saveAndClose() {
        val finalTitle = title.trim().ifBlank { task.title }
        val finalRepeat = if (dueDate == null) RepeatRule.None else repeatRule
        val changed = finalTitle != task.title ||
            notes != task.notes ||
            dueDate != task.dueDate ||
            projectId != task.projectId ||
            important != task.important ||
            finalRepeat != task.repeatRule
        // Closing an untouched task must not bump updatedAt and reshuffle lists.
        if (changed) onSave(finalTitle, notes, dueDate, projectId, important, finalRepeat)
        onClose()
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(taskId) {
        runCatching { focusRequester.requestFocus() }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(chromeColor(0.96f))
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                val isEscape = event.key == Key.Escape
                if (isEscape && event.type == KeyEventType.KeyDown) {
                    saveAndClose()
                    true
                } else {
                    false
                }
            },
    ) {
        DetailsHeader(
            title = title.ifBlank { "Без названия" },
            closeable = true,
            onClose = ::saveAndClose,
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
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

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TaskCheckbox(
                    checked = task.done,
                    onCheckedChange = { onToggleDone() },
                    circleSize = 26.dp,
                    touchSize = 44.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (task.done) "Завершено" else "Не завершено",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (task.done) DoneColor else MaterialTheme.colorScheme.onSurfaceVariant,
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

            Spacer(Modifier.height(20.dp))

            SectionCard {
                SectionTitle("Срок выполнения")
                DueDateChipRow(
                    dueDate = dueDate,
                    today = today,
                    onMenu = { dateMenuOpen = true },
                    onClear = {
                        dueDate = null
                        // Without an anchor date the rule cannot fire.
                        repeatRule = RepeatRule.None
                    },
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))

                SectionTitle("Повтор")
                RepeatPickerRow(
                    rule = repeatRule,
                    enabled = dueDate != null,
                    onMenu = { repeatMenuOpen = true },
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))

                SectionTitle("Проект")
                ProjectPickerRow(
                    projects = projects,
                    selectedId = projectId,
                    onMenu = { projectMenuOpen = true },
                    onNewProject = onNewProject,
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Заметки")
            TextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Добавьте заметку") },
                singleLine = false,
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { /* focus stays */ }),
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 12.dp),
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
                if (!isDialog) {
                    Text(
                        text = "Esc — закрыть",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(12.dp))
                }
                FilledTonalButton(
                    onClick = { saveAndClose() },
                    enabled = title.isNotBlank(),
                ) {
                    Text("Готово")
                }
            }
        }

        DueDateDropdown(
            expanded = dateMenuOpen,
            today = today,
            dueDate = dueDate,
            onDismiss = { dateMenuOpen = false },
            onPick = {
                dueDate = it
                dateMenuOpen = false
            },
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
private fun DetailsHeader(title: String, closeable: Boolean, onClose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (closeable) {
            IconButton(onClick = onClose) {
                TfIcon(
                    icon = TfIcon.Close,
                    size = 20.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Rounded group for the "when / repeat / project" properties, To Do style. */
@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun DueDateChipRow(
    dueDate: String?,
    today: String,
    onMenu: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TfIcon(
            icon = TfIcon.Today,
            size = 20.dp,
            tint = if (dueDate == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = dueDate?.let { formatFullDate(it) } ?: "Нет срока",
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                dueDate == null -> MaterialTheme.colorScheme.onSurfaceVariant
                dueDate < today -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.weight(1f),
        )
        if (dueDate != null) {
            TextButton(onClick = onClear) {
                Text(
                    "Убрать",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.error),
                )
            }
        }
        TextButton(onClick = onMenu) { Text(if (dueDate == null) "Выбрать" else "Изменить") }
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
        Spacer(Modifier.width(10.dp))
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
                .size(9.dp)
                .background(parseProjectColor(selected?.colorHex ?: "#4F6BED"), CircleShape),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = selected?.name ?: "Без проекта",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onNewProject) { Text("Новый") }
        TextButton(onClick = onMenu) { Text("Изменить") }
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
            leadingIcon = {
                if (selectedId == null) {
                    TfIcon(
                        icon = TfIcon.Check,
                        size = 16.dp,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            onClick = {
                onPick(null)
                onDismiss()
            },
        )
        HorizontalDivider()
        projects.forEach { project ->
            DropdownMenuItem(
                text = { Text(project.name) },
                leadingIcon = {
                    Box(
                        Modifier
                            .size(9.dp)
                            .background(parseProjectColor(project.colorHex), CircleShape),
                    )
                },
                onClick = {
                    onPick(project.id)
                    onDismiss()
                },
            )
        }
    }
}
