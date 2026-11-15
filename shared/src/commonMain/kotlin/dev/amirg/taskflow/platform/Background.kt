package dev.amirg.taskflow.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Entry point for choosing a background picture.
 *
 * The window shell owns the native file dialog, so it fills [launch] in; while
 * it stays `null` the UI hides the option completely. That keeps the shared UI
 * free of platform file APIs and lets Android ship without the feature yet.
 */
object BackgroundPicker {
    var launch by mutableStateOf<((destination: String, onResult: (Boolean) -> Unit) -> Unit)?>(null)
}

/** Decodes the imported picture, or `null` when the file is missing or broken. */
expect fun loadBackgroundImage(path: String): ImageBitmap?

/** Deletes the imported picture. A missing file is not an error. */
expect fun deleteBackgroundImage(path: String)
