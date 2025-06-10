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
import xyz.lilsus.papp.data.model.WalletError
import xyz.lilsus.papp.data.model.WalletPaymentSendResponse
import xyz.lilsus.papp.data.model.WalletResult
import xyz.lilsus.papp.data.repository.ConnectedWalletRepository
import xyz.lilsus.papp.util.Invoice

// TODO: Probably better to put the wallet repository in constructor!
class MainViewModel() : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private var scannedQrCode: String? = null

    private val _paymentResult =
        MutableStateFlow<WalletResult<WalletPaymentSendResponse>?>(null)   // API response or error message
    val paymentResult: StateFlow<WalletResult<WalletPaymentSendResponse>?> = _paymentResult

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    val wallet = ConnectedWalletRepository.Builder()
        .withBlink()
        .build()

    fun onQrCodeDetected(qr: String) {
        if (scannedQrCode == null) {
            scannedQrCode = qr
            val bolt11Invoice = Invoice.parseOrNull(qr)
            if (bolt11Invoice != null) {
                _showBottomSheet.value = true
                payInvoice(bolt11Invoice)
            } else {
                // TODO: Debounce
                scannedQrCode = null
                println("TODO: not a bolt11 invoice. show hints")
            }
        }
    }

    private fun payInvoice(paymentRequest: Invoice) {
        viewModelScope.launch {
            _paymentResult.value = null

            val result = try {
                val results: List<WalletResult<WalletPaymentSendResponse>> =
                    wallet.payBolt11(paymentRequest)
                // FIXME: this is weird
                val first = results.firstOrNull()
                if (first == null) {
                    // Map to error
                    WalletResult.Failure(WalletError.Unexpected("No wallet responded"))
                }
                first
            } catch (e: Exception) {
                println("Payment error: ${e.message}")
                // FIXME: error should not be null result
                null
            }

            _paymentResult.value = result
        }
    }


    fun dismissQrCode() {
        scannedQrCode = null
        _paymentResult.value = null
        _showBottomSheet.value = false
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
