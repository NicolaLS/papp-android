package xyz.lilsus.papp.presentation.main.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

// TODO: Give the QR elevation on active, and animate / remove elevantion on inactive/detected.
// Todo:
// https://chatgpt.com/share/68afd551-b68c-800d-88ea-f26aaf28e0d7


// TODO:
// Test staggered delays
// Test different easing
// Find out how we can interrupt and transition to compressed animation
// Test randomized scaling below 1x e.g A: 0.98-1.05 B: 1-1.2 C: 0.95-1.15
// Find out performance best practices
// Check if animating the color could be cool to


// Canvas drawing performance considerations
// 1. Avoid reading state e.g val x = mystate.value
// 2. Don't allocate objects per frame inside canvas e.g Paint Rect Sha

// Detected clench animation
// - Shrink outer squares below 1x
// - Shrink inner squares to 1x in case they were bigger bcs of idle anim
// - Move sqaures inward:
//  - top-left: x * 2, y * 2, top-right: x * -2, y * 2
//  - bottom-left: x * 2, y * -2, bottom-right: x * -2, y * -2
// note I can get the current scale with scales[i].value
enum class QrState { INACTIVE, ACTIVE, DETECTED }

enum class QrSquareSizes(val size: Float) {
    OUTER(0.34f),
    INNER(0.25f),
}
// Square size is relative to canvas size (1 = 100% Canvas Size)

// Scale applied to the square size when icon is compressed.
const val COMPRESSED_SQUARE_SCALE = 0.8f

// How much the gap should be reduced 1 = gap stays the same, 2 = gap halfes, maybe this should not be called factor lol
const val COMPRESSED_GAP_FACTOR = 4f


// TODO: Does the stroke go outwards or inwards? ie is it also relevant for margin?
// TODO: Check how to easiest create the design in a clean extensible way, maybe
// do different types, filled, outlined, container
// TODO: put the const vals to the fitting objects / classes

data class QrSquarePosition(
    val x: Float,
    val y: Float,
) {
    companion object {
    }

}

fun QrSquarePosition.transformTowardsCenter(step: Float): QrSquarePosition {
    fun adjust(value: Float): Float =
        when {
            value < 0.5f -> value + step
            value > 0.5f -> value - step
            else -> value
        }

    return this.copy(
        x = adjust(this.x),
        y = adjust(this.y)
    )
}


class QrSquare(position: QrSquarePosition, val sizeType: QrSquareSizes, val filled: Boolean) {
    companion object {
        const val MAX_SCALE_IDLE = 1.2f
        const val MIN_SCALE_IDLE = 0.95f

        const val OUTER_SQUARE_SIZE = 0.34f
        const val INNER_SQUARE_SIZE = 0.2f

        val TOP_LEFT = QrSquarePosition(0f, 0f)
        val TOP_LEFT_INNER = QrSquarePosition(
            0f + (OUTER_SQUARE_SIZE * 1 / 2) - (INNER_SQUARE_SIZE * 1 / 2),
            0f + (OUTER_SQUARE_SIZE * 1 / 2) - (INNER_SQUARE_SIZE * 1 / 2),
        )
        val TOP_RIGHT = QrSquarePosition(1f - OUTER_SQUARE_SIZE, 0f)
        val BOTTOM_LEFT = QrSquarePosition(0f, 1f - OUTER_SQUARE_SIZE)
        val BOTTOM_RIGHT = QrSquarePosition(1f - OUTER_SQUARE_SIZE, 1f - OUTER_SQUARE_SIZE)
    }


    val size = sizeType.size
    val scale = Animatable(1f)

    // Add margin
    val mPos = position.transformTowardsCenter(max(MAX_SCALE_IDLE - 1f, 0f) * 1 / 2 * size)

    val x = Animatable(mPos.x)
    val y = Animatable(mPos.y)

    fun getCenter(): QrSquarePosition {
        return QrSquarePosition(this.x.value + this.size * 0.5f, this.y.value + this.size * 0.5f)
    }

}


@Composable
fun AnimatedAbstractQr(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    state: QrState = QrState.ACTIVE,
) {
    val squares = remember {
        val topLeftOut = QrSquare(QrSquare.TOP_LEFT, QrSquareSizes.OUTER, false)
        val topLeftOutCenter = topLeftOut.getCenter()
        val topLeftInPos = QrSquarePosition(
            topLeftOutCenter.x - (QrSquareSizes.INNER.size / 2),
            topLeftOutCenter.y - (QrSquareSizes.INNER.size / 2),
        )
        val topLeftIn = QrSquare(
            topLeftInPos,
            QrSquareSizes.INNER,
            true
        )
        val bottomLeftOut = QrSquare(QrSquare.BOTTOM_LEFT, QrSquareSizes.OUTER, false)
        val bottomRightOut = QrSquare(QrSquare.BOTTOM_RIGHT, QrSquareSizes.OUTER, true)
        val topRightOut = QrSquare(QrSquare.TOP_RIGHT, QrSquareSizes.OUTER, false)

        listOf(
            topLeftOut,
            topLeftIn,
            bottomLeftOut,
            bottomRightOut,
            topRightOut,
        )
    }


    // TODO: Use the constants min max idle and compressed scale
    LaunchedEffect(state) {
        if (state == QrState.ACTIVE) {
            squares.forEach {
                launch {
                    val targetValue = Random.nextFloat() * 0.15f + 1.05f // 1.05 to 1.2
                    it.scale.animateTo(
                        targetValue = targetValue,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                // durationMillis = Random.nextInt(1300, 1800),
                                // TODO: 1. Check if also random speed looks nicer, I think it does not.
                                // TODO: 2. Maybe make speed faster for larger expand and slower for smaller expand
                                // 1.05 * x = 1400 = 1334 so 1.05 will have 1400 speed, and 1.2 will have 1600
                                durationMillis = (1334 * targetValue).toInt(),
                                easing = EaseInOutCubic
                            ),


                            repeatMode = RepeatMode.Reverse
                        ),
                    )
                }
            }
        } else if (state == QrState.DETECTED) {
            squares.forEach {
                val (newX, newY) = it.mPos.transformTowardsCenter((0.5f * (1f - COMPRESSED_SQUARE_SCALE) * QrSquare.OUTER_SQUARE_SIZE) * COMPRESSED_GAP_FACTOR)
                launch {
                    it.x.animateTo(
                        newX,
                        animationSpec = tween(600, easing = EaseInOutCubic)
                    )
                }
                launch {
                    it.y.animateTo(
                        newY,
                        animationSpec = tween(600, easing = EaseInOutCubic)
                    )
                }
                launch {
                    it.scale.animateTo(
                        targetValue = 0.8f,
                        animationSpec = tween(600, easing = EaseInOutCubic)

                    )
                }

            }
        }
    }





    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.Black, size = size)
            val canvasSize = size.minDimension


            // Add another scale here with center of canvas pivot for the clench
            // scale(0.5f, pivot = center) {
            squares.forEachIndexed { index, spec ->
                val px = spec.x.value * canvasSize
                val py = spec.y.value * canvasSize

                val s = spec.size * canvasSize
                val size = Size(s, s)
                val cornerRadius = CornerRadius(s * 0.1f)
                val center = Offset(px + s / 2, py + s / 2)
                val stroke = Stroke(width = s * 0.1f)

                // TODO: Check if withTransform(scale, translate) { ... } is better.
                scale(spec.scale.value, pivot = center) {
                    translate(left = px, top = py) {
                        if (spec.filled) {
                            drawRoundRect(
                                color = color,
                                size = size,
                                cornerRadius = cornerRadius
                            )
                        } else {
                            drawRoundRect(
                                color = color,
                                size = size,
                                cornerRadius = cornerRadius,
                                style = stroke
                            )
                        }

                    }
                }
            }
        }
    }
}