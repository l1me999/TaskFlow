package dev.amirg.taskflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.model.Task
import dev.amirg.taskflow.platform.todayIsoDate
import kotlinx.coroutines.launch

@Composable
fun App(store: TaskStore) {
    TaskFlowTheme {
        TaskFlowApp(store)
    }
}

@Composable
private fun TaskFlowApp(store: TaskStore) {
    val today = remember { todayIsoDate() }
    var view by remember { mutableStateOf<TaskView>(TaskView.Today) }
    var openTaskId by remember { mutableStateOf<String?>(null) }
    var showNewProject by remember { mutableStateOf(false) }
    var deleteTaskId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val onAttachClick: () -> Unit = {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Прикрепление файлов появится в следующем обновлении")
        }
    }

    val openTask = store.data.tasks.firstOrNull { it.id == openTaskId }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxWidth < 840.dp

        if (compact) {
            MobileLayout(
                store = store,
                today = today,
                view = view,
                openTask = openTask,
                snackbarHostState = snackbarHostState,
                onSelectView = { view = it },
                onOpenTask = { openTaskId = it },
                onNewProject = { showNewProject = true },
                onDeleteTask = { deleteTaskId = it },
                onAttachClick = onAttachClick,
            )
        } else {
            DesktopLayout(
                store = store,
                today = today,
                view = view,
                openTask = openTask,
                snackbarHostState = snackbarHostState,
                onSelectView = { view = it },
                onOpenTask = { openTaskId = it },
                onNewProject = { showNewProject = true },
                onDeleteTask = { deleteTaskId = it },
                onAttachClick = onAttachClick,
            )
        }

        if (openTask != null) {
            // Only the compact layout shows details as a dialog; on desktop it is a pane.
            if (compact) {
                TaskDetailsDialog(
                    task = openTask,
                    projects = store.data.projects,
                    today = today,
                    onSave = { title, notes, dueDate, projectId, important ->
                        store.updateTask(openTask, title, notes, dueDate, projectId, important)
                    },
                    onDelete = { deleteTaskId = openTask.id },
                    onClose = { openTaskId = null },
                    onAttach = onAttachClick,
                )
            }
        }
    }

    if (showNewProject) {
        NewProjectDialog(
            onCreate = { name ->
                store.addProject(name)
                showNewProject = false
            },
            onDismiss = { showNewProject = false },
        )
    }

    val taskToDelete: Task? = deleteTaskId?.let { id -> store.data.tasks.firstOrNull { it.id == id } }
    if (taskToDelete != null) {
        ConfirmDeleteDialog(
            taskTitle = taskToDelete.title,
            onConfirm = {
                store.deleteTask(taskToDelete.id)
                if (openTaskId == taskToDelete.id) openTaskId = null
                deleteTaskId = null
            },
            onDismiss = { deleteTaskId = null },
        )
    }
}

@Composable
private fun DesktopLayout(
    store: TaskStore,
    today: String,
    view: TaskView,
    openTask: Task?,
    snackbarHostState: SnackbarHostState,
    onSelectView: (TaskView) -> Unit,
    onOpenTask: (String?) -> Unit,
    onNewProject: () -> Unit,
    onDeleteTask: (String) -> Unit,
    onAttachClick: () -> Unit,
) {
    val data = store.data

    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Sidebar(
                modifier = Modifier
                    .width(288.dp)
                    .fillMaxHeight(),
                data = data,
                view = view,
                today = today,
                onSelect = onSelectView,
                onNewProject = onNewProject,
            )

            VerticalSeparator()

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                ScreenHeader(title = view.title(data), subtitle = view.subtitle(data, today))
                HorizontalSeparator()

                if (view is TaskView.Projects) {
                    ProjectsScreen(
                        modifier = Modifier.weight(1f),
                        data = data,
                        onOpenProject = { onSelectView(TaskView.Project(it)) },
                        onNewProject = onNewProject,
                    )
                } else {
                    TaskListContent(
                        modifier = Modifier.weight(1f),
                        data = data,
                        view = view,
                        today = today,
                        onOpen = onOpenTask,
                        onToggle = { store.toggleTask(it) },
                        onToggleImportant = { store.toggleImportant(it) },
                        onDelete = onDeleteTask,
                    )
                    HorizontalSeparator()
                    QuickAddBar(
                        today = today,
                        onAttach = onAttachClick,
                        onSubmit = { title, dueDate, important ->
                            store.addTask(
                                title = title,
                                projectId = (view as? TaskView.Project)?.id,
                                dueDate = dueDate,
                                important = important,
                            )
                        },
                    )
                }
            }

            if (openTask != null) {
                VerticalSeparator()
                TaskDetailsPane(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight(),
                    task = openTask,
                    data = data,
                    today = today,
                    onSave = { title, notes, dueDate, projectId, important ->
                        store.updateTask(openTask, title, notes, dueDate, projectId, important)
                    },
                    onDelete = { onDeleteTask(openTask.id) },
                    onClose = { onOpenTask(null) },
                    onAttach = onAttachClick,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun MobileLayout(
    store: TaskStore,
    today: String,
    view: TaskView,
    openTask: Task?,
    snackbarHostState: SnackbarHostState,
    onSelectView: (TaskView) -> Unit,
    onOpenTask: (String?) -> Unit,
    onNewProject: () -> Unit,
    onDeleteTask: (String) -> Unit,
    onAttachClick: () -> Unit,
) {
    val data = store.data
    val showBack = view is TaskView.Project

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MobileTopBar(
                title = view.title(data),
                subtitle = view.subtitle(data, today),
                showBack = showBack,
                onBack = { onSelectView(TaskView.Projects) },
            )
        },
        bottomBar = {
            Column(Modifier.imePadding()) {
                QuickAddBar(
                    today = today,
                    onAttach = onAttachClick,
                    onSubmit = { title, dueDate, important ->
                        store.addTask(
                            title = title,
                            projectId = (view as? TaskView.Project)?.id,
                            dueDate = dueDate,
                            important = important,
                        )
                    },
                )
                MobileNavigationBar(view = view, onSelect = onSelectView)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (view is TaskView.Projects) {
                ProjectsScreen(
                    modifier = Modifier.fillMaxSize(),
                    data = data,
                    onOpenProject = { onSelectView(TaskView.Project(it)) },
                    onNewProject = onNewProject,
                )
            } else {
                TaskListContent(
                    modifier = Modifier.fillMaxSize(),
                    data = data,
                    view = view,
                    today = today,
                    onOpen = onOpenTask,
                    onToggle = { store.toggleTask(it) },
                    onToggleImportant = { store.toggleImportant(it) },
                    onDelete = onDeleteTask,
                )
            }
        }
    }
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 14.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MobileTopBar(
    title: String,
    subtitle: String,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 60.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Text("←")
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class BottomDestination(
    val view: TaskView,
    val label: String,
    val icon: String,
)

private val BottomDestinations = listOf(
    BottomDestination(TaskView.Today, "Сегодня", "📅"),
    BottomDestination(TaskView.Important, "Важно", "★"),
    BottomDestination(TaskView.Planned, "План", "📅"),
    BottomDestination(TaskView.All, "Все", "📋"),
    BottomDestination(TaskView.Projects, "Проекты", "📁"),
)

@Composable
private fun MobileNavigationBar(view: TaskView, onSelect: (TaskView) -> Unit) {
    val selectedIndex = when (view) {
        is TaskView.Project -> 4
        else -> BottomDestinations.indexOfFirst { it.view == view }
    }

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomDestinations.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(destination.view) },
                icon = { Text(destination.icon) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun VerticalSeparator() {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun HorizontalSeparator() {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
