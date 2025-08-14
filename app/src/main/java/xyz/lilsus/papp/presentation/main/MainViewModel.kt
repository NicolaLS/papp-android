package xyz.lilsus.papp.presentation.main

import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.di.QrCodeAnalyzer
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase


sealed class PaymentUiState {
    object Loading : PaymentUiState()
    data class Received(val result: SendPaymentData) : PaymentUiState()
    data class Error(val message: String?) : PaymentUiState()
}

// TODO: DI with Hilt
class MainViewModel(
    val payUseCase: PayInvoiceUseCase,
    val qrCodeAnalyzer: QrCodeAnalyzer,
    val cameraController: LifecycleCameraController,
) : ViewModel() {
    private val _uiState = mutableStateOf<PaymentUiState?>(null)
    val uiState: State<PaymentUiState?> = _uiState

    init {
        qrCodeAnalyzer.qrCodeFlow
            .onEach { qr ->
                if (!qrCodeAnalyzer.isProcessingFlag.get()) {
                    processQr(qr)
                }
            }.launchIn(viewModelScope)
    }

    private fun processQr(qr: String) {
        if (qrCodeAnalyzer.isProcessingFlag.compareAndSet(false, true)) {
            val bolt11Invoice = Invoice.parseOrNull(qr) ?: return
            payUseCase(bolt11Invoice)
                .onEach { result ->
                    _uiState.value = when (result) {
                        is Resource.Loading -> PaymentUiState.Loading
                        is Resource.Success -> PaymentUiState.Received(result.data.first)
                        is Resource.Error -> PaymentUiState.Error(result.message)
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun reset() {
        _uiState.value = null
        qrCodeAnalyzer.isProcessingFlag.set(false)
    }
}