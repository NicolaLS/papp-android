package xyz.lilsus.papp.presentation.main

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.common.InvoiceAnalyzer
import xyz.lilsus.papp.common.InvoiceParser
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean


sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class Received(val result: SendPaymentData) : PaymentUiState()
    data class Error(val message: String?) : PaymentUiState()
}

// TODO: DI with Hilt
class MainViewModel(
    val payUseCase: PayInvoiceUseCase,
    val imageAnalysisUseCase: ImageAnalysis,
    val invoiceAnalyzer: InvoiceAnalyzer,
    val analyzerExecutor: ExecutorService,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val walletRepositoryFlow = application.appDependencies.walletRepositoryFlow
                val analyzerExecutor = application.appDependencies.analyzerExecutor
                val barcodeScannerExecutor = application.appDependencies.barcodeScannerExecutor

                val payUseCase = PayInvoiceUseCase(walletRepositoryFlow)

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

                val qrCodeScanner = BarcodeScanning.getClient(
                    BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                )
                val invoiceParser = InvoiceParser()
                val analyzer = InvoiceAnalyzer(barcodeScannerExecutor, qrCodeScanner, invoiceParser)

                imageAnalysisUseCase.setAnalyzer(analyzerExecutor, analyzer)

                MainViewModel(
                    payUseCase,
                    imageAnalysisUseCase,
                    analyzer,
                    analyzerExecutor
                )
            }
        }
    }

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


    private var cameraProvider: ProcessCameraProvider? = null


    // FIXME: This feel wrong. Maybe redesign the whole analyzer/parser stuff.
    init {
        invoiceAnalyzer.invoiceParser.onBolt11(::pay)
    }

    fun bindCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraProvider = ProcessCameraProvider.awaitInstance(context)
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageAnalysisUseCase
            )
        }
    }

    // TODO: Handle amount-less bolt11
    fun pay(bolt11Invoice: Bolt11Invoice) {
        if (isProcessingFlag.compareAndSet(false, true)) {
            imageAnalysisUseCase.clearAnalyzer()
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
        imageAnalysisUseCase.setAnalyzer(analyzerExecutor, invoiceAnalyzer)
        _uiState.value = PaymentUiState.Idle
        isProcessingFlag.set(false)
    }

    override fun onCleared() {
        super.onCleared()
        analyzerExecutor.shutdownNow()
        invoiceAnalyzer.executor.shutdownNow()
    }
}