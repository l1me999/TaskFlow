package dev.amirg.taskflow.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amirg.taskflow.platform.epochMillisFromIsoDateUtc
import dev.amirg.taskflow.platform.isoDateFromEpochMillisUtc

/**
 * Always-visible input at the bottom of the list. Only the title is required:
 * due date and the "important" flag are optional extras chosen from the buttons
 * on the right, exactly like Microsoft To Do.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBar(
    today: String,
    onAttach: () -> Unit,
    onSubmit: (title: String, dueDate: String?, important: Boolean) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<String?>(null) }
    var important by remember { mutableStateOf(false) }
    var dateMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun submit() {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        onSubmit(trimmed, dueDate, important)
        title = ""
        dueDate = null
        important = false
        runCatching { focusRequester.requestFocus() }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (dueDate != null || important) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dueDate != null) {
                        PropertyChip(
                            icon = "📅",
                            label = formatDueDate(dueDate!!, today),
                            onClear = { dueDate = null },
                        )
                    }
                    if (important) {
                        PropertyChip(
                            icon = "★",
                            label = "Важно",
                            onClear = { important = false },
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
            }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 22.sp,
                    ),
                )
                Spacer(Modifier.width(10.dp))

                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onPreviewKeyEvent { event ->
                            val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
                            if (isEnter && event.type == KeyEventType.KeyDown) {
                                submit()
                                true
                            } else {
                                false
                            }
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = "Добавить задачу",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Box {
                    IconButton(onClick = { dateMenuOpen = true }) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (dueDate != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontSize = 21.sp,
                            ),
                        )
                    }
                    DueDateDropdown(
                        expanded = dateMenuOpen,
                        today = today,
                        dueDate = dueDate,
                        onDismiss = { dateMenuOpen = false },
                        onPick = { dueDate = it },
                        onCustomDate = {
                            dateMenuOpen = false
                            showDatePicker = true
                        },
                    )
                }

                IconButton(onClick = onAttach) {
                    Text(
                        text = "📎",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 21.sp,
                        ),
                    )
                }

                IconButton(onClick = { important = !important }) {
                    Text(
                        text = if (important) "★" else "☆",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (important) ImportantColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 21.sp,
                        ),
                    )
                }

                Spacer(Modifier.width(4.dp))

                FilledIconButton(
                    onClick = { submit() },
                    enabled = title.isNotBlank(),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                ) {
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 20.sp),
                    )
                }
            }
        }
    }

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

@Composable
private fun PropertyChip(
    icon: String,
    label: String,
    onClear: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape,
    ) {
        Row(
            Modifier.padding(start = 10.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                ),
            )
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    ),
                )
            }
        }
    }
}

/**
 * Shared "when is it due" menu: three shortcuts, a calendar, and a way to clear.
 * Used by the quick input and by the details pane.
 */
@Composable
internal fun DueDateDropdown(
    expanded: Boolean,
    today: String,
    dueDate: String?,
    onDismiss: () -> Unit,
    onPick: (String?) -> Unit,
    onCustomDate: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Сегодня") },
            onClick = {
                onPick(today)
                onDismiss()
            },
        )
        DropdownMenuItem(
            text = { Text("Завтра") },
            onClick = {
                isoDatePlusDays(today, 1)?.let(onPick)
                onDismiss()
            },
        )
        DropdownMenuItem(
            text = { Text("Через неделю") },
            onClick = {
                isoDatePlusDays(today, 7)?.let(onPick)
                onDismiss()
            },
        )
        DropdownMenuItem(
            text = { Text("Выбрать дату…") },
            onClick = onCustomDate,
        )
        if (dueDate != null) {
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Убрать срок") },
                onClick = {
                    onPick(null)
                    onDismiss()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerSheet(
    initialIsoDate: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = epochMillisFromIsoDateUtc(initialIsoDate),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onPick(isoDateFromEpochMillisUtc(it)) }
                },
                enabled = state.selectedDateMillis != null,
            ) {
                Text("Готово")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}
