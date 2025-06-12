package xyz.lilsus.papp.presentation.main.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Composable
fun AnimatedErrorIcon(modifier: Modifier = Modifier) {
    val strokeWidth = 6f
    val errorColor = Color(0xFFF44336)
    val circleColor = errorColor.copy(alpha = 0.2f)

    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            progress = value
        }
    }

    Canvas(modifier = modifier.size(96.dp)) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2, height / 2)
        val radius = width.coerceAtMost(height) / 2 - strokeWidth

        // Background circle
        drawCircle(
            color = circleColor,
            radius = radius,
            center = center
        )

        // Line 1: top-left to bottom-right
        val start1 = Offset(width * 0.3f, height * 0.3f)
        val end1 = Offset(width * 0.7f, height * 0.7f)

        // Line 2: top-right to bottom-left
        val start2 = Offset(width * 0.7f, height * 0.3f)
        val end2 = Offset(width * 0.3f, height * 0.7f)

        val half = 0.5f

        // Animate line 1 first, then line 2
        if (progress <= half) {
            val p = progress / half
            val current = Offset(
                lerp(start1.x, end1.x, p),
                lerp(start1.y, end1.y, p)
            )
            drawLine(
                color = errorColor,
                start = start1,
                end = current,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        } else {
            // Draw full line 1
            drawLine(
                color = errorColor,
                start = start1,
                end = end1,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Animate line 2
            val p = (progress - half) / half
            val current = Offset(
                lerp(start2.x, end2.x, p),
                lerp(start2.y, end2.y, p)
            )
            drawLine(
                color = errorColor,
                start = start2,
                end = current,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}