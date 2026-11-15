package dev.amirg.taskflow.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF2564CF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E6FF),
    onPrimaryContainer = Color(0xFF00204D),
    secondary = Color(0xFF4F6BED),
    onSecondary = Color.White,
    background = Color(0xFFFAF9F8),
    onBackground = Color(0xFF1F1F1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFF0EFEE),
    onSurfaceVariant = Color(0xFF5B5957),
    outline = Color(0xFF797774),
    outlineVariant = Color(0xFFE1DFDD),
    error = Color(0xFFC42B1C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0A2E5C),
    primaryContainer = Color(0xFF1B3A63),
    onPrimaryContainer = Color(0xFFD8E6FF),
    secondary = Color(0xFF9FB4FF),
    onSecondary = Color(0xFF152A66),
    background = Color(0xFF1B1B1B),
    onBackground = Color(0xFFEFEFEF),
    surface = Color(0xFF242424),
    onSurface = Color(0xFFEFEFEF),
    surfaceVariant = Color(0xFF2F2F2F),
    onSurfaceVariant = Color(0xFFC9C7C5),
    outline = Color(0xFF8C8C8C),
    outlineVariant = Color(0xFF3F3F3F),
    error = Color(0xFFFF99A0),
)

/** Slightly larger than Material defaults: comfortable on a laptop and a phone. */
private val AppTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 13.sp),
    labelSmall = TextStyle(fontSize = 11.sp),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** True while a user-chosen background image is shown behind the UI. */
internal val LocalHasBackgroundImage = staticCompositionLocalOf { false }

@Composable
fun TaskFlowTheme(
    background: ImageBitmap? = null,
    content: @Composable () -> Unit,
) {
    // A photo background always uses the dark palette: white text over a dimmed
    // picture is readable, dark text on a light theme over the same picture is not.
    val colorScheme = if (isSystemInDarkTheme() || background != null) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(LocalHasBackgroundImage provides (background != null)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colorScheme.background),
            ) {
                if (background != null) {
                    Image(
                        bitmap = background,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    // Keeps the list text readable wherever the picture is bright.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.62f)),
                    )
                    // The screen title has no panel of its own, so it gets its own
                    // shade: bright photos would otherwise wash the header out.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Black.copy(alpha = 0.45f),
                                    0.22f to Color.Transparent,
                                ),
                            ),
                    )
                }
                // A root Surface is what gives every Text a readable default color.
                // Without it Compose falls back to black content color, which disappears
                // on the dark background.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    contentColor = colorScheme.onBackground,
                    content = content,
                )
            }
        }
    }
}

/**
 * Tint for chrome that sits on top of the background (sidebar, input bar, details
 * pane). A photo background needs to show through, so panels turn translucent;
 * on a plain background they stay opaque, as before.
 */
@Composable
internal fun chromeColor(alpha: Float = 0.80f): Color =
    if (LocalHasBackgroundImage.current) {
        MaterialTheme.colorScheme.surface.copy(alpha = alpha)
    } else {
        MaterialTheme.colorScheme.surface
    }
