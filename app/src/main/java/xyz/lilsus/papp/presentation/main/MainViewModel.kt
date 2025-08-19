package xyz.lilsus.papp.presentation.main

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.common.InvoiceParser
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean


sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Received(val result: SendPaymentData) : PaymentUiState()
    data class Error(val message: String?) : PaymentUiState()
}

// TODO: DI with Hilt
class MainViewModel(val payUseCase: PayInvoiceUseCase) : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    // NOTE: There are two concurrent things happening in our QR code detection logic:
    // - The Image Analysis Use Case running on some Executor (not main thread)
    // - The MlKit Barcode Scanner processing some image it received in the `ImageAnalysis.Analyzer`
    //   implementation. (Main thread).
    // It is good to understand that this is somewhat sync. because of the default:
    // `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` the Image Analysis Use Case drops frames
    // until the analyzer calls `imageProxy.close()` i.e is "finished". So the barcode scanner
    // **will not be invoked** concurrently because of this. (if:)
    // Then it depends on when we close the image proxy, right now we do this **after** the barcode
    // scanner is finished processing, this means we actually get a sync. pipeline.
    // We **could** close the imageProxy immediately and then pass a copy to the barcode scanner
    // so it processes multiple frames concurrently. It might be interesting to test if this can
    // improve performance.
    // For now we do the sync. sequential approach, we need some indicator to stop scanning though,
    // and right now it could be a simple boolean or other value because we scan on the main thread
    // but it is better if we are explicit and use an Atomic Boolean.
    // See: https://developer.android.com/reference/androidx/camera/core/ImageAnalysis#setAnalyzer(java.util.concurrent.Executor,androidx.camera.core.ImageAnalysis.Analyzer)
    // Side note: I am not sure if the executor passed in `ImageAnalysis.setAnalyzer()` is the same as
    // ImageAnalyzer.Builder().setBackgroundExecutor()...but I assume so...
    private val isProcessingFlag = AtomicBoolean(false)

    private val _uiState = mutableStateOf<PaymentUiState>(PaymentUiState.Idle)
    val uiState: State<PaymentUiState> = _uiState


    val invoiceParser = InvoiceParser().onBolt11(::pay)

    // TODO: Handle amount-less bolt11
    fun pay(bolt11Invoice: Bolt11Invoice) {
        if (isProcessingFlag.compareAndSet(false, true)) {
            payUseCase(bolt11Invoice).onEach { result ->
                _uiState.value = when (result) {
                    is Resource.Loading -> PaymentUiState.Loading
                    is Resource.Success -> PaymentUiState.Received(result.data.first)
                    is Resource.Error -> PaymentUiState.Error(result.message)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun reset() {
        _uiState.value = PaymentUiState.Idle
        isProcessingFlag.set(false)
    }

    private val previewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { request ->
            _surfaceRequest.value = request
        }
    }

    suspend fun bindToCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        val analyzer = MlKitAnalyzer(
            listOf(scanner),
            ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
            // NOTE: This is the executor for the barcode scanner
            ContextCompat.getMainExecutor(context)
        ) { result ->
            val barcodes = result?.getValue(scanner)
            barcodes?.firstOrNull()?.rawValue.let {
                invoiceParser.parse(it)
            }
        }

        val analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(Executors.newCachedThreadPool()) { imageProxy ->
                    if (isProcessingFlag.get()) {
                        imageProxy.close()
                    } else {
                        // NOTE: MlKitAnalyzer closes the image proxy for us after finish.
                        analyzer.analyze(imageProxy)
                    }
                }
            }

        val provider = ProcessCameraProvider.awaitInstance(context)
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            previewUseCase,
            analysisUseCase
        )

        try {
            awaitCancellation()
        } finally {
            provider.unbindAll()
        }
    }
}