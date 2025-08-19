package xyz.lilsus.papp.presentation.main.components

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay
import xyz.lilsus.papp.R
import xyz.lilsus.papp.common.InvoiceAnalyzer
import xyz.lilsus.papp.common.InvoiceParser
import xyz.lilsus.papp.presentation.main.MainViewModel
import xyz.lilsus.papp.presentation.main.PaymentUiState
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(viewModel: MainViewModel, onSettingsClick: () -> Unit) {
    // Create a Image Analysis use case (+Barcode Scanner)
    // Configure use case and attach to this life-cycle
    val localContext = LocalContext.current

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val invoiceAnalyzer = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val qrCodeScanner = BarcodeScanning.getClient(options)
        val invoiceParser = InvoiceParser()
            .onBolt11(viewModel::pay)
        InvoiceAnalyzer(qrCodeScanner, invoiceParser)
    }
    val imageAnalysisUseCase = remember {
        val imageAnalysisUseCase = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        // NOTE: MlKit recommends around 1920x1080 resolution which is 16:9.
                        // But we do not need a WYSIWYG experience so we prefer the most common
                        // native sensor aspect ratio which is 4:3.
                        ResolutionStrategy(
                            Size(1920, 1440),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER,
                        )
                    )
                    .build()
            )
            .build()
        // TODO: setAnalyzer
        imageAnalysisUseCase
    }

    fun reset() {
        imageAnalysisUseCase.setAnalyzer(Executors.newCachedThreadPool(), invoiceAnalyzer)
        viewModel.reset()
    }

    fun rebindCameraProvider() {
        cameraProvider?.let { cameraProvider ->
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                localContext as LifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageAnalysisUseCase
            )
        }
    }


    val uiState by viewModel.uiState
    val done = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true, // allow partial
        confirmValueChange = { newState ->
            // Only allow dismiss if done
            newState != SheetValue.Hidden || done.value
        }
    )

    val showBottomSheet = when (uiState) {
        is PaymentUiState.Loading,
        is PaymentUiState.Received,
        is PaymentUiState.Error -> true

        PaymentUiState.Idle -> false
        is PaymentUiState.Detected -> false
    }



    LaunchedEffect(uiState) {
        when (uiState) {
            is PaymentUiState.Idle -> imageAnalysisUseCase.setAnalyzer(
                Executors.newCachedThreadPool(),
                invoiceAnalyzer
            )

            is PaymentUiState.Detected -> {
                // Confirm required:
                // - Swap QR with the amount to confirm
                // - Show fee loading
            }

            is PaymentUiState.Loading -> {
                done.value = false
            }

            is PaymentUiState.Received,
            is PaymentUiState.Error -> {
                delay(500) // prevent accidental dismiss
                done.value = true
            }
        }
        if (uiState !is PaymentUiState.Idle) {
            imageAnalysisUseCase.clearAnalyzer()
        }
    }
    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider.awaitInstance(localContext)
        imageAnalysisUseCase.setAnalyzer(Executors.newCachedThreadPool(), invoiceAnalyzer)
        rebindCameraProvider()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        BreathingQrIcon()
        RandomQrMessage()
    }

    if (showBottomSheet) {
        QrCodeBottomSheet(
            uiState = uiState,
            onDismiss = ::reset,
            done = done.value,
            sheetState = sheetState,
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

@Composable
fun BreathingQrIcon() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Icon(
        painter = painterResource(id = R.drawable.qr_icon),
        contentDescription = "QR Icon",
        tint = Color.Black,
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}

@Composable
fun RandomQrMessage() {
    val context = LocalContext.current
    val messages = context.resources.getStringArray(R.array.qr_scan_messages)

    // Pick once per launch
    val message = remember { messages.random() }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    )
}