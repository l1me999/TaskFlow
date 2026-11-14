package dev.amirg.taskflow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The app's icon set.
 *
 * Emoji were only ever a stand-in: they render differently on every platform and
 * look out of place next to Material text. These are plain vector paths drawn on
 * a 24x24 grid, so they scale, follow the theme color and need no extra
 * dependency (`material-icons-extended` is not available offline).
 */
enum class TfIcon {
    Today,
    Planned,
    Star,
    StarFilled,
    All,
    Folder,
    Attach,
    Send,
    Back,
    More,
    Expand,
    Collapse,
    Trash,
    Edit,
    Close,
    Plus,
    Check,
}

@Composable
fun TfIcon(
    icon: TfIcon,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = LocalContentColor.current,
) {
    Canvas(modifier.size(size)) {
        val factor = this.size.minDimension / 24f
        scale(factor, factor, pivot = Offset.Zero) {
            drawTfIcon(icon, tint)
        }
    }
}

private fun DrawScope.drawTfIcon(icon: TfIcon, color: Color) {
    val stroke = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (icon) {
        TfIcon.Today -> drawCalendar(color, stroke, withDots = true)
        TfIcon.Planned -> drawCalendar(color, stroke, withDots = false)
        TfIcon.Star -> drawPath(starPath(), color, style = stroke)
        TfIcon.StarFilled -> drawPath(starPath(), color)
        TfIcon.All -> drawList(color, stroke)
        TfIcon.Folder -> drawPath(folderPath(), color, style = stroke)
        TfIcon.Attach -> drawPaperclip(color, stroke)
        TfIcon.Send -> {
            drawLine(color, Offset(4f, 12f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(12.5f, 5.5f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(12.5f, 18.5f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
        }

        TfIcon.Back -> {
            drawLine(color, Offset(20f, 12f), Offset(5f, 12f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(11.5f, 5.5f), Offset(5f, 12f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(11.5f, 18.5f), Offset(5f, 12f), stroke.width, StrokeCap.Round)
        }

        TfIcon.More -> {
            drawCircle(color, radius = 1.7f, center = Offset(12f, 5f))
            drawCircle(color, radius = 1.7f, center = Offset(12f, 12f))
            drawCircle(color, radius = 1.7f, center = Offset(12f, 19f))
        }

        TfIcon.Expand -> {
            drawLine(color, Offset(5.5f, 9f), Offset(12f, 15.5f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(18.5f, 9f), Offset(12f, 15.5f), stroke.width, StrokeCap.Round)
        }

        TfIcon.Collapse -> {
            drawLine(color, Offset(9f, 5.5f), Offset(15.5f, 12f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(9f, 18.5f), Offset(15.5f, 12f), stroke.width, StrokeCap.Round)
        }

        TfIcon.Trash -> drawTrash(color, stroke)
        TfIcon.Edit -> drawPencil(color, stroke)
        TfIcon.Close -> {
            drawLine(color, Offset(6f, 6f), Offset(18f, 18f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(18f, 6f), Offset(6f, 18f), stroke.width, StrokeCap.Round)
        }

        TfIcon.Plus -> {
            drawLine(color, Offset(12f, 5f), Offset(12f, 19f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(5f, 12f), Offset(19f, 12f), stroke.width, StrokeCap.Round)
        }

        TfIcon.Check -> {
            drawLine(color, Offset(5f, 12.5f), Offset(10f, 17.5f), stroke.width, StrokeCap.Round)
            drawLine(color, Offset(10f, 17.5f), Offset(19f, 6.5f), stroke.width, StrokeCap.Round)
        }
    }
}

private fun DrawScope.drawCalendar(color: Color, stroke: Stroke, withDots: Boolean) {
    drawRoundRect(
        color = color,
        topLeft = Offset(3.5f, 5f),
        size = Size(17f, 15.5f),
        cornerRadius = CornerRadius(3.5f, 3.5f),
        style = stroke,
    )
    drawLine(color, Offset(3.5f, 9.5f), Offset(20.5f, 9.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(8f, 2.5f), Offset(8f, 6.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(16f, 2.5f), Offset(16f, 6.5f), stroke.width, StrokeCap.Round)

    if (withDots) {
        drawCircle(color, radius = 1.5f, center = Offset(8.5f, 14.5f))
        drawCircle(color, radius = 1.5f, center = Offset(12.5f, 14.5f))
        drawCircle(color, radius = 1.5f, center = Offset(16.5f, 14.5f))
    } else {
        // Planned is a plain calendar: the dot grid is what tells "today" apart.
        drawLine(color, Offset(7f, 14.5f), Offset(10.5f, 14.5f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(13.5f, 14.5f), Offset(17f, 14.5f), stroke.width, StrokeCap.Round)
    }
}

private fun DrawScope.drawList(color: Color, stroke: Stroke) {
    var y = 6.5f
    repeat(3) {
        drawCircle(color, radius = 1.5f, center = Offset(5f, y))
        drawLine(color, Offset(9.5f, y), Offset(19.5f, y), stroke.width, StrokeCap.Round)
        y += 5.5f
    }
}

private fun DrawScope.drawTrash(color: Color, stroke: Stroke) {
    drawLine(color, Offset(3.5f, 6.5f), Offset(20.5f, 6.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(9f, 3.5f), Offset(15f, 3.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(8f, 3.5f), Offset(8f, 6.5f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(16f, 3.5f), Offset(16f, 6.5f), stroke.width, StrokeCap.Round)

    val body = Path().apply {
        moveTo(5.5f, 6.5f)
        lineTo(6.7f, 19.5f)
        cubicTo(6.8f, 20.3f, 7.4f, 20.8f, 8.2f, 20.8f)
        lineTo(15.8f, 20.8f)
        cubicTo(16.6f, 20.8f, 17.2f, 20.3f, 17.3f, 19.5f)
        lineTo(18.5f, 6.5f)
    }
    drawPath(body, color, style = stroke)
    drawLine(color, Offset(10f, 10f), Offset(10.4f, 17f), stroke.width, StrokeCap.Round)
    drawLine(color, Offset(14f, 10f), Offset(13.6f, 17f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawPencil(color: Color, stroke: Stroke) {
    val body = Path().apply {
        moveTo(4f, 20f)
        lineTo(4.9f, 15.8f)
        lineTo(15.2f, 5.5f)
        lineTo(18.5f, 8.8f)
        lineTo(8.2f, 19.1f)
        close()
    }
    drawPath(body, color, style = stroke)
    drawLine(color, Offset(14.2f, 6.5f), Offset(17.5f, 9.8f), stroke.width, StrokeCap.Round)
}

private fun DrawScope.drawPaperclip(color: Color, stroke: Stroke) {
    // A rounded clip seen at a 45 degree angle is just two nested stadiums.
    rotate(degrees = -45f) {
        drawRoundRect(
            color = color,
            topLeft = Offset(8.5f, 2.5f),
            size = Size(7f, 16.5f),
            cornerRadius = CornerRadius(3.5f, 3.5f),
            style = stroke,
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(10f, 7.5f),
            size = Size(4f, 8.5f),
            cornerRadius = CornerRadius(2f, 2f),
            style = stroke,
        )
    }
}

private fun starPath(): Path {
    val centerX = 12f
    val centerY = 12.5f
    val outer = 9.2f
    val inner = 4f
    val path = Path()

    repeat(10) { index ->
        val radius = if (index % 2 == 0) outer else inner
        val angle = (-90.0 + index * 36.0) * PI / 180.0
        val x = centerX + (radius * cos(angle)).toFloat()
        val y = centerY + (radius * sin(angle)).toFloat()
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun folderPath(): Path = Path().apply {
    moveTo(3.5f, 7.5f)
    lineTo(3.5f, 18f)
    cubicTo(3.5f, 19.1f, 4.4f, 20f, 5.5f, 20f)
    lineTo(18.5f, 20f)
    cubicTo(19.6f, 20f, 20.5f, 19.1f, 20.5f, 18f)
    lineTo(20.5f, 9.5f)
    cubicTo(20.5f, 8.4f, 19.6f, 7.5f, 18.5f, 7.5f)
    lineTo(11.8f, 7.5f)
    lineTo(9.4f, 4.8f)
    cubicTo(9.0f, 4.4f, 8.5f, 4.2f, 8.0f, 4.2f)
    lineTo(5.5f, 4.2f)
    cubicTo(4.4f, 4.2f, 3.5f, 5.1f, 3.5f, 6.2f)
    close()
}
