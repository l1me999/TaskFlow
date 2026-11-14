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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.amirg.taskflow.model.Project
import dev.amirg.taskflow.model.TaskFlowData
import dev.amirg.taskflow.model.TaskScope
import dev.amirg.taskflow.model.openCount
import dev.amirg.taskflow.model.openCountForProject

@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    data: TaskFlowData,
    view: TaskView,
    today: String,
    onSelect: (TaskView) -> Unit,
    onNewProject: () -> Unit,
) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 18.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "TaskFlow",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(18.dp))

        SidebarSection(
            view = view,
            scope = TaskScope.Today,
            label = "Сегодня",
            icon = "📅",
            count = data.openCount(TaskScope.Today, today),
            onSelect = onSelect,
        )
        SidebarSection(
            view = view,
            scope = TaskScope.Important,
            label = "Важно",
            icon = "★",
            count = data.openCount(TaskScope.Important, today),
            onSelect = onSelect,
        )
        SidebarSection(
            view = view,
            scope = TaskScope.Planned,
            label = "Планируемое",
            icon = "📆",
            count = data.openCount(TaskScope.Planned, today),
            onSelect = onSelect,
        )
        SidebarSection(
            view = view,
            scope = TaskScope.All,
            label = "Все задачи",
            icon = "📋",
            count = data.openCount(TaskScope.All, today),
            onSelect = onSelect,
        )

        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Spacer(Modifier.height(10.dp))

        // Clicking the section title opens the project list, which is the only
        // place where projects can be renamed, recolored or deleted.
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onSelect(TaskView.Projects) }
                .background(
                    if (view == TaskView.Projects) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    } else {
                        Color.Transparent
                    },
                )
                .padding(start = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Проекты",
                style = MaterialTheme.typography.labelLarge,
                color = if (view == TaskView.Projects) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onNewProject) {
                Text("+")
            }
        }

        if (data.projects.isEmpty()) {
            Text(
                text = "Пока нет проектов",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(data.projects, key = { it.id }) { project ->
                    SidebarProject(
                        project = project,
                        selected = view is TaskView.Project && view.id == project.id,
                        count = data.openCountForProject(project.id),
                        onClick = { onSelect(TaskView.Project(project.id)) },
                    )
                }
                item(key = "new-project") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNewProject)
                            .heightIn(min = 40.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "+",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Новый проект",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarSection(
    view: TaskView,
    scope: TaskScope,
    label: String,
    icon: String,
    count: Int,
    onSelect: (TaskView) -> Unit,
) {
    val target: TaskView = when (scope) {
        TaskScope.Today -> TaskView.Today
        TaskScope.Important -> TaskView.Important
        TaskScope.Planned -> TaskView.Planned
        TaskScope.All -> TaskView.All
        TaskScope.Project -> TaskView.All
    }
    val selected = view == target

    Row(
        Modifier
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onSelect(target) }
            .heightIn(min = 44.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun SidebarProject(
    project: Project,
    selected: Boolean,
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .heightIn(min = 40.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(9.dp)
                .background(parseProjectColor(project.colorHex), CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = project.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    data: TaskFlowData,
    onOpenProject: (String) -> Unit,
    onNewProject: () -> Unit,
    onEditProject: (Project) -> Unit,
    onDeleteProject: (Project) -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            FilledTonalButton(onClick = onNewProject) {
                Text("+")
                Spacer(Modifier.width(8.dp))
                Text("Новый проект")
            }
        }

        if (data.projects.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "📁",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text("Проектов пока нет", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Создайте проект, чтобы группировать задачи.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(data.projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        count = data.openCountForProject(project.id),
                        onClick = { onOpenProject(project.id) },
                        onEdit = { onEditProject(project) },
                        onDelete = { onDeleteProject(project) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    count: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(12.dp)
                    .background(parseProjectColor(project.colorHex), CircleShape),
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(project.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "$count ${pluralRu(count, "задача", "задачи", "задач")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Text(
                        text = "⋮",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 21.sp,
                        ),
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Изменить") },
                        leadingIcon = { Text("✎") },
                        onClick = {
                            menuOpen = false
                            onEdit()
                        },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Text("🗑", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

/** Palette offered when a project is created or edited, in To Do-like tones. */
val ProjectColors = listOf(
    "#4F6BED", // blue
    "#C239B3", // magenta
    "#E3008C", // pink
    "#D13438", // red
    "#CA5010", // orange
    "#986F0B", // ochre
    "#498205", // green
    "#00B7C3", // teal
    "#486991", // steel
    "#69797E", // grey
)

/** Turns `#RRGGBB` from the storage file into a color, falling back to the default. */
fun parseProjectColor(hex: String): Color = runCatching {
    val value = hex.removePrefix("#")
    if (value.length != 6) return@runCatching Color(0xFF4F6BED)
    Color(0xFF000000L or value.toLong(16))
}.getOrDefault(Color(0xFF4F6BED))
