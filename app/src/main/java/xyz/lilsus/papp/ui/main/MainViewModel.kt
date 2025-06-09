package xyz.lilsus.papp.ui.main

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.mlkit.vision.MlKitAnalyzer
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
import kotlinx.coroutines.launch
import xyz.lilsus.papp.data.ApiRepository
import xyz.lilsus.papp.data.PaymentSendPayload

class MainViewModel(private val apiRepository: ApiRepository = ApiRepository()) : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val _scannedQrCode = MutableStateFlow<String?>(null)
    val scannedQrCode: StateFlow<String?> = _scannedQrCode

    private val _paymentResult =
        MutableStateFlow<PaymentSendPayload?>(null)   // API response or error message
    val paymentResult: StateFlow<PaymentSendPayload?> = _paymentResult

    private val _isPayingInvoice = MutableStateFlow(false)
    val isPayingInvoice: StateFlow<Boolean> = _isPayingInvoice

    fun onQrCodeDetected(qr: String) {
        if (_scannedQrCode.value == null && !_isPayingInvoice.value) {
            _scannedQrCode.value = qr
            // TODO: only attempt to pay ln invoices or bitcoin:xxx?ln=xxx
            // TODO: because blink does not respond with amount paid, we'll need to
            // parse the invoice and remember the amount -.-
            // try to get them to respond with amount paid and fee paid
            payInvoice(qr)
        }
    }

    private fun payInvoice(paymentRequest: String) {
        viewModelScope.launch {
            _isPayingInvoice.value = true
            _paymentResult.value = null

            val result = try {
                apiRepository.payInvoice(paymentRequest)
            } catch (e: Exception) {
                println("Payment error: ${e.message}")
                // FIXME: error should not be null result
                null
            }

            _paymentResult.value = result
            _isPayingInvoice.value = false
        }
    }


    fun dismissQrCode() {
        _scannedQrCode.value = null
        _paymentResult.value = null
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
                    if (_scannedQrCode.value != null) {
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
