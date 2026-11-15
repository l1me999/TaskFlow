package dev.amirg.taskflow.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.amirg.taskflow.data.TaskStore
import dev.amirg.taskflow.platform.BackgroundPicker
import dev.amirg.taskflow.platform.loadBackgroundImage
import dev.amirg.taskflow.ui.App
import okio.Path.Companion.toOkioPath
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

private val BackgroundExtensions = listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif")

fun main() {
    val dataDir = File(System.getProperty("user.home"), ".taskflow").apply { mkdirs() }
    val store = TaskStore(dataDir.resolve("taskflow.json").toOkioPath())
    // One fixed name keeps the picture next to the data and independent of where
    // the original file was: moving or cleaning up that file must not break the app.
    val backgroundFile = dataDir.resolve("background.img")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "TaskFlow",
            // Compose would otherwise draw its own logo in the title bar; the
            // app is identified by the title alone.
            icon = BitmapPainter(ImageBitmap(1, 1)),
            state = rememberWindowState(width = 1100.dp, height = 760.dp),
        ) {
            // The native file dialog needs the window, and only the shell has it.
            LaunchedEffect(Unit) {
                BackgroundPicker.launch = { destination, onResult ->
                    onResult(importBackground(window, destination))
                }
            }

            App(store, backgroundPath = backgroundFile.absolutePath)
        }
    }
}

/**
 * Asks for a picture and imports a copy as [destination]. The copy is validated
 * first and only then replaces the current background, so a bad pick cannot
 * destroy a working one.
 */
private fun importBackground(parent: Frame, destination: String): Boolean {
    val dialog = FileDialog(parent, "Выберите фоновое изображение", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        val lower = name.lowercase()
        BackgroundExtensions.any { lower.endsWith(it) }
    }
    dialog.isVisible = true

    val name = dialog.file ?: return false
    val directory = dialog.directory ?: return false
    val source = File(directory, name)
    val target = File(destination)
    val temp = File(target.parentFile, "background.tmp")

    val decoded = runCatching {
        source.copyTo(temp, overwrite = true)
        loadBackgroundImage(temp.absolutePath) != null
    }.getOrDefault(false)

    if (decoded) {
        runCatching { temp.copyTo(target, overwrite = true) }
    }
    temp.delete()
    return decoded
}
