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
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase


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

    private var scannedQrCode: String? = null

    private val _uiState = mutableStateOf<PaymentUiState>(PaymentUiState.Idle)
    val uiState: State<PaymentUiState> = _uiState


    fun onQrCodeDetected(qr: String) {
        if (scannedQrCode == null) {
            scannedQrCode = qr
            val bolt11Invoice = Invoice.parseOrNull(qr)
            if (bolt11Invoice != null) {
                pay(bolt11Invoice)
            } else {
                // TODO: Debounce
                scannedQrCode = null
                println("TODO: not a bolt11 invoice. show hints")
            }
        }
    }

    fun pay(invoice: Invoice) {
        payUseCase(invoice).onEach { result ->
            _uiState.value = when (result) {
                is Resource.Loading -> PaymentUiState.Loading
                is Resource.Success -> PaymentUiState.Received(result.data.first)
                is Resource.Error -> PaymentUiState.Error(result.message)
            }
        }.launchIn(viewModelScope)
    }

    fun reset() {
        _uiState.value = PaymentUiState.Idle
        scannedQrCode = null
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
            ContextCompat.getMainExecutor(context)
        ) { result ->
            val barcodes = result?.getValue(scanner)
            barcodes?.firstOrNull()?.rawValue?.let(::onQrCodeDetected)
        }

        val analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    if (scannedQrCode != null) {
                        imageProxy.close()
                    } else {
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