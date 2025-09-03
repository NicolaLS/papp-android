package xyz.lilsus.papp.presentation.main

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.QrCodeAnalyzer
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.use_case.wallets.InvoiceConfirmationData
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase
import xyz.lilsus.papp.domain.use_case.wallets.ProbeFeeUseCase
import xyz.lilsus.papp.domain.use_case.wallets.ShouldConfirmPaymentResult
import xyz.lilsus.papp.domain.use_case.wallets.ShouldConfirmPaymentUseCase
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean


// FIXME: Having these classes here is awkward...but the whole business logic regarding
// the model/errors for the repos/use cases is messed up right now. So this is just to get
// it to work and I'll clean that up later.

sealed class PaymentResult {
    data class Success(val data: Pair<SendPaymentData, WalletTypeEntry>) : PaymentResult()
    data class Error(val message: String?) : PaymentResult()
}


sealed class Intent {
    object Dismiss : Intent()
    data class BindCamera(val context: Context, val lifecycleOwner: LifecycleOwner) : Intent()
    data class PayInvoice(val invoice: Invoice.Bolt11) : Intent()
}

sealed class UiState {
    object Active : UiState()
    data class QrDetected(val invalidInvoice: Invoice.Invalid?) : UiState()

    data class ConfirmPayment(val data: InvoiceConfirmationData) : UiState()

    object PerformingPayment : UiState()
    data class PaymentDone(val result: PaymentResult) : UiState()
}

class MainViewModel(
    val payUseCase: PayInvoiceUseCase,
    val shouldConfirmPayment: ShouldConfirmPaymentUseCase,
    val imageAnalysisUseCase: ImageAnalysis,
    val invoiceAnalyzer: QrCodeAnalyzer,
    val analyzerExecutor: ExecutorService,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val settingsRepository = application.appDependencies.settingsRepository
                val walletRepositoryFlow = application.appDependencies.walletRepositoryFlow
                val analyzerExecutor = application.appDependencies.analyzerExecutor
                val barcodeScannerExecutor = application.appDependencies.barcodeScannerExecutor

                val payUseCase = PayInvoiceUseCase(walletRepositoryFlow)
                val probeFeeUseCase = ProbeFeeUseCase(walletRepositoryFlow)
                val shouldConfirmPaymentUseCase =
                    ShouldConfirmPaymentUseCase(settingsRepository, probeFeeUseCase)

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
                val analyzer = QrCodeAnalyzer(barcodeScannerExecutor, qrCodeScanner)

                imageAnalysisUseCase.setAnalyzer(analyzerExecutor, analyzer)

                MainViewModel(
                    payUseCase,
                    shouldConfirmPaymentUseCase,
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

    // TODO: Check if removing the atomic boolean and just using the main thread for the
    // barcode scanner executor improves performance.
    private val isProcessingFlag = AtomicBoolean(false)

    private var cameraProvider: ProcessCameraProvider? = null

    var uiState by mutableStateOf<UiState>(UiState.Active)
        private set

    init {
        invoiceAnalyzer.onQrCodeDetected {
            // Callback is called from another thread (not main thread).
            if (isProcessingFlag.compareAndSet(false, true)) {
                imageAnalysisUseCase.clearAnalyzer()
                onQrDetected(it)
            }
        }
    }

    fun dispatch(intent: Intent) {
        when (intent) {
            Intent.Dismiss -> reset()
            is Intent.BindCamera -> bindCamera(intent.context, intent.lifecycleOwner)
            is Intent.PayInvoice -> executePaymentProposal(intent.invoice)
        }
    }

    private fun bindCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraProvider = ProcessCameraProvider.awaitInstance(context)
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageAnalysisUseCase
            )
        }
    }

    private fun reset() {
        uiState = UiState.Active
        imageAnalysisUseCase.setAnalyzer(analyzerExecutor, invoiceAnalyzer)
        isProcessingFlag.set(false)
    }

    private fun onQrDetected(rawQr: String) {
        Invoice.parse(rawQr).also {
            when (it) {
                is Invoice.Bolt11 -> {
                    uiState = UiState.QrDetected(null)
                    approvePaymentProposal(it)
                }
                // TODO: Workflow for Invoices without amount (or LNURLs)
                // For now, treat amount-less invoice as invalid.
                is Invoice.Invalid -> {
                    uiState = UiState.QrDetected(it)
                }
            }
        }
    }

    private fun approvePaymentProposal(invoice: Invoice.Bolt11) {
        viewModelScope.launch {
            when (val confirm = shouldConfirmPayment(invoice)) {
                ShouldConfirmPaymentResult.ConfirmationNotRequired -> {
                    executePaymentProposal(invoice)
                }

                is ShouldConfirmPaymentResult.ConfirmationRequired -> {
                    uiState = UiState.ConfirmPayment(confirm.data)
                }
            }
        }
    }

    private fun executePaymentProposal(confirmedInvoice: Invoice.Bolt11) {
        payUseCase(confirmedInvoice).onEach {
            uiState = when (it) {
                is Resource.Error -> UiState.PaymentDone(PaymentResult.Error(it.message))
                is Resource.Loading<*> -> UiState.PerformingPayment
                is Resource.Success<Pair<SendPaymentData, WalletTypeEntry>> -> UiState.PaymentDone(
                    PaymentResult.Success(it.data)
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        analyzerExecutor.shutdownNow()
        invoiceAnalyzer.executor.shutdownNow()
    }
}