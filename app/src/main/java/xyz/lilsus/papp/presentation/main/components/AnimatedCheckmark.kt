package xyz.lilsus.papp.presentation.main.components

import android.graphics.PathMeasure
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCheckmark(modifier: Modifier = Modifier) {
    val strokeWidth = 6f
    val checkColor = Color(0xFF4CAF50)
    val circleColor = checkColor.copy(alpha = 0.2f)

    var progress by remember { mutableFloatStateOf(0f) }

    // Start the animation when this Composable is first shown
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

        // Define full checkmark path
        val path = Path().apply {
            moveTo(width * 0.28f, height * 0.53f)
            lineTo(width * 0.45f, height * 0.70f)
            lineTo(width * 0.75f, height * 0.32f)
        }

        // Animate partial drawing of the path
        val androidPath = path.asAndroidPath()
        val measure = PathMeasure(androidPath, false)
        val length = measure.length
        val dstAndroid = android.graphics.Path()
        measure.getSegment(0f, length * progress, dstAndroid, true)
        val drawnPath = dstAndroid.asComposePath()

        drawPath(
            path = drawnPath,
            color = checkColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}