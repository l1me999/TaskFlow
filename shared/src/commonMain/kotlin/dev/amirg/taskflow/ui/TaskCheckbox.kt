package dev.amirg.taskflow.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Round done checkbox: an empty ring while the task is open, a filled green
 * circle with a white tick once it is completed.
 *
 * Material's [androidx.compose.material3.Checkbox] draws a square and stretches
 * into an oval when resized; this one is drawn from primitives so it stays a
 * perfect circle at any size.
 */
@Composable
fun TaskCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    circleSize: Dp = 24.dp,
    touchSize: Dp = 44.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "doneProgress",
    )

    val ringColor = MaterialTheme.colorScheme.outline
    val fill = lerp(Color.Transparent, DoneColor, progress)
    val ring = lerp(if (hovered) DoneColor.copy(alpha = 0.75f) else ringColor, DoneColor, progress)

    Box(
        modifier = modifier
            .size(touchSize)
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox,
                interactionSource = interaction,
                indication = null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(circleSize)
                .graphicsLayer {
                    val scale = 0.92f + 0.08f * progress
                    scaleX = if (hovered && !checked) 1.06f else scale
                    scaleY = if (hovered && !checked) 1.06f else scale
                }
                .background(fill, CircleShape)
                .border(width = 2.dp, color = ring, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TfIcon(
                icon = TfIcon.Check,
                size = circleSize * 0.62f,
                tint = Color.White.copy(alpha = progress),
            )
        }
    }
}
