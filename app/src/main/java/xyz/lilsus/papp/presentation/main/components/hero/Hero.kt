package xyz.lilsus.papp.presentation.main.components.hero

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate

data class SquareSpec(
    val x: Float,
    val y: Float,
    val size: Float,
)

@Composable
fun Hero(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val squares = remember {
        listOf(
            SquareSpec(0f + 0.1f, 0f + 0.1f, 0.3f),
            SquareSpec(1f - (0.3f + 0.1f), 0f + 0.1f, 0.3f),
            SquareSpec(0f + 0.1f, 1f - (0.3f + 0.1f), 0.3f),
            SquareSpec(1f - (0.3f + 0.1f), 1f - (0.3f + 0.1f), 0.3f),
        )
    }
    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension

            squares.forEachIndexed { index, spec ->
                val px = spec.x * canvasSize
                val py = spec.y * canvasSize

                val s = spec.size * canvasSize
                val size = Size(s, s)

                translate(left = px, top = py) {
                    drawRoundRect(
                        color = color,
                        size = size,
                    )

                }
            }
        }
    }
}