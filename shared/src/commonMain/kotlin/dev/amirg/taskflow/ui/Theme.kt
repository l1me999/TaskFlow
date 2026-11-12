package dev.amirg.taskflow.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF3D5AFE)

@Composable
fun TaskFlowTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFF8FA8FF))
    } else {
        lightColorScheme(primary = Accent)
    }

    MaterialTheme(colorScheme = colors, content = content)
}
