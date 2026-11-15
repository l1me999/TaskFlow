package dev.amirg.taskflow.platform

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

// Android has no picker wired up yet (the shell would need an ActivityResult
// launcher), but an already imported file can still be shown once that lands.

actual fun loadBackgroundImage(path: String): ImageBitmap? = runCatching {
    BitmapFactory.decodeFile(path)?.asImageBitmap()
}.getOrNull()

actual fun deleteBackgroundImage(path: String) {
    runCatching { File(path).delete() }
}
