package xyz.lilsus.papp.presentation.main.components

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import kotlin.math.pow

@Composable
fun CameraPreview(cameraController: LifecycleCameraController) {
    val scope = rememberCoroutineScope()
    val zoomRatio = remember { Animatable(1f) }
    var totalDrag = 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount

                        // Exponential scaling for faster zoom at larger pulls
                        val dragFactor = (totalDrag / 300f).coerceIn(-1f, 4f)
                        val target = (1f + dragFactor.pow(3)).coerceIn(1f, 5f)

                        scope.launch {
                            zoomRatio.snapTo(target)
                            cameraController.setZoomRatio(target)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            zoomRatio.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = { fraction ->
                                        // ease-out: slows down near the end
                                        1f - (1f - fraction) * (1f - fraction)
                                    }
                                )
                            ) {
                                // this = Animatable<Float, *>
                                cameraController.setZoomRatio(this.value)
                            }
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    this.controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}