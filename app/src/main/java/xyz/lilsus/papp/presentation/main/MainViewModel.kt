package xyz.lilsus.papp.presentation.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase
import java.util.concurrent.atomic.AtomicBoolean


sealed class PaymentUiState {
    object Idle : PaymentUiState()
    data class Detected(val bolt11Invoice: Bolt11Invoice) : PaymentUiState()
    object Loading : PaymentUiState()
    data class Received(val result: SendPaymentData) : PaymentUiState()
    data class Error(val message: String?) : PaymentUiState()
}

// TODO: DI with Hilt
class MainViewModel(val payUseCase: PayInvoiceUseCase) : ViewModel() {
    private val _uiState = mutableStateOf<PaymentUiState>(PaymentUiState.Idle)
    val uiState: State<PaymentUiState> = _uiState

    val isProcessingFlag = AtomicBoolean(false)

    fun pay(invoice: Bolt11Invoice) {
        if (isProcessingFlag.compareAndSet(false, true)) {
            payUseCase(invoice).onEach { result ->
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
}