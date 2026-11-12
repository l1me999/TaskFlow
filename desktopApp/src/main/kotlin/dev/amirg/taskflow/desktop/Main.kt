package dev.amirg.taskflow.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.ui.App
import okio.Path.Companion.toOkioPath
import java.io.File

fun main() {
    val store = createStore()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "TaskFlow",
            state = rememberWindowState(width = 1100.dp, height = 760.dp),
        ) {
            App(store)
        }
    }
}

private fun createStore(): TaskStore {
    val dataDir = File(System.getProperty("user.home"), ".taskflow").apply { mkdirs() }
    return TaskStore(dataDir.resolve("taskflow.json").toOkioPath())
}
