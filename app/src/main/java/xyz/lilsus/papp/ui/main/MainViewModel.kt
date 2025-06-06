package xyz.lilsus.papp.ui.main

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.lilsus.papp.data.ApiRepository

class MainViewModel(    private val apiRepository: ApiRepository = ApiRepository()) : ViewModel() {
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val _scannedQrCode = MutableStateFlow<String?>(null)
    val scannedQrCode: StateFlow<String?> = _scannedQrCode

    fun onQrCodeDetected(qr: String) {
        if (_scannedQrCode.value == null) {
            // Launch coroutine to fetch API data and update scannedQrCode
            viewModelScope.launch {
                val apiResponse = try {
                    apiRepository.fetchDataFromApi()
                } catch (e: Exception) {
                    "API Error: ${e.message}"
                }
                _scannedQrCode.value = apiResponse
            }
        }
    }

    fun dismissQrCode() {
        _scannedQrCode.value = null
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
