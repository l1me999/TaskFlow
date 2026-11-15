package dev.amirg.taskflow.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.io.File

actual fun loadBackgroundImage(path: String): ImageBitmap? = runCatching {
    Image.makeFromEncoded(File(path).readBytes()).toComposeImageBitmap()
}.getOrNull()

actual fun deleteBackgroundImage(path: String) {
    runCatching { File(path).delete() }
}
