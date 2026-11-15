package dev.amirg.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
    defaultDueDate: String? = null,
    defaultImportant: Boolean = false,
    onSubmit: (title: String, dueDate: String?, important: Boolean) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember(defaultDueDate) { mutableStateOf(defaultDueDate) }
    var important by remember(defaultImportant) { mutableStateOf(defaultImportant) }
    var dateMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun submit() {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        onSubmit(trimmed, dueDate, important)
        title = ""
        dueDate = defaultDueDate
        important = defaultImportant
        runCatching { focusRequester.requestFocus() }
    }

    Surface(
        color = chromeColor(0.92f),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (dueDate != null || important) {
                Row(
                    Modifier.padding(start = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dueDate != null) {
                        PropertyChip(
                            icon = TfIcon.Today,
                            label = formatDueDate(dueDate!!, today),
                            onClear = { dueDate = null },
                        )
                    }
                    if (important) {
                        PropertyChip(
                            icon = TfIcon.StarFilled,
                            label = "Важно",
                            onClear = { important = false },
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(start = 18.dp, end = 4.dp)
                    .heightIn(min = 52.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                        TfIcon(
                            icon = TfIcon.Today,
                            size = 21.dp,
                            tint = if (dueDate != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
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
                    TfIcon(
                        icon = TfIcon.Attach,
                        size = 21.dp,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = { important = !important }) {
                    TfIcon(
                        icon = if (important) TfIcon.StarFilled else TfIcon.Star,
                        size = 21.dp,
                        tint = if (important) ImportantColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.width(4.dp))

                FilledIconButton(
                    onClick = { submit() },
                    enabled = title.isNotBlank(),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                ) {
                    TfIcon(icon = TfIcon.Send, size = 20.dp)
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
    icon: TfIcon,
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
            TfIcon(
                icon = icon,
                size = 15.dp,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                TfIcon(
                    icon = TfIcon.Close,
                    size = 14.dp,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
