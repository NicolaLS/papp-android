package xyz.lilsus.papp.presentation.main

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.common.InvoiceAnalyzer
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
class MainViewModel(
    val payUseCase: PayInvoiceUseCase,
    val imageAnalysisUseCase: ImageAnalysis,
    val invoiceAnalyzer: InvoiceAnalyzer
) : ViewModel() {
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
    private val analyzerExecutor = Executors.newCachedThreadPool()


    // FIXME: This feel wrong. Maybe redesign the whole analyzer/parser stuff.
    init {
        invoiceAnalyzer.invoiceParser.onBolt11(::pay)
    }

    fun rebindCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraProvider = ProcessCameraProvider.awaitInstance(context)
            // FIXME: This **hides** a bug / messy setup:
            // - In DI we create new instances of the use case when navigating
            // - We never clean up, in fact the view model is never destroyed
            // - The old instance gets cleaned up I think, so then there is a non
            // functional / null use case attached.
            // - We bind to the MainActivity lifecycle not the view model / component.
            // need to fix DI and Navigation later...
            cameraProvider?.unbindAll()
            imageAnalysisUseCase.setAnalyzer(analyzerExecutor, invoiceAnalyzer)
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
}