package xyz.lilsus.papp.ui.main

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onSettingsClick: () -> Unit) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    if (cameraPermission.status.isGranted) {
        MainScreenContent(viewModel, onSettingsClick)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val text = if (cameraPermission.status.shouldShowRationale) {
                "We need your camera to scan QR codes. Please grant permission!"
            } else {
                "Hi there! Grant camera permission to get started."
            }
            Text(text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                Text("Grant Camera Access")
            }
        }
    }
}

@Composable
fun MainScreenContent(viewModel: MainViewModel, onSettingsClick: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val scannedQrCode by viewModel.scannedQrCode.collectAsStateWithLifecycle()
    val paymentResult by viewModel.paymentResult.collectAsStateWithLifecycle()
    val isPayingInvoice by viewModel.isPayingInvoice.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(surfaceRequest = request, modifier = Modifier.fillMaxSize())
    }

    if (scannedQrCode != null) {
        QrCodeBottomSheet(
            scannedQrCode = scannedQrCode!!,
            paymentResult = paymentResult,
            isLoading = isPayingInvoice,
            onDismiss = viewModel::dismissQrCode
        )
    }

    // Settings Button at top-left corner
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeBottomSheet(
    scannedQrCode: String,
    paymentResult: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val done = remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            // Allow dismiss only if done
            newState != SheetValue.Hidden || done.value
        }
    )

    LaunchedEffect(paymentResult) {
        if (paymentResult != null) {
            // Delay a bit to avoid accidental dismissal.
            kotlinx.coroutines.delay(1000)
            done.value = true
        }
    }



    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        // Block system back press unless done
        BackHandler(enabled = done.value == false) {}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading && paymentResult == null) {
                // TODO: Show spinner while waiting for payment response
                CircularProgressIndicator()
            } else {
                // Handle Payment Result
                AnimatedCheckmark(modifier = Modifier.size(150.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    paymentResult!!,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }


            }
        }
    }
}

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
        val measure = android.graphics.PathMeasure(androidPath, false)
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
