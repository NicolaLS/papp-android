package xyz.lilsus.papp.presentation.settings.screens.payments

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.repository.SettingsRepository


data class PaymentsUIState(
    val alwaysConfirmPayments: Boolean,
    val confirmPaymentsAbove: Float
)

class PaymentsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val settingsRepository = application.appDependencies.settingsRepository
                PaymentsViewModel(
                    settingsRepository
                )
            }
        }
    }

    private val _uiState = mutableStateOf<PaymentsUIState?>(null)
    val uiState: State<PaymentsUIState?> = _uiState

    init {
        settingsRepository.paymentSettings.onEach {
            _uiState.value = PaymentsUIState(
                it.alwaysConfirmPayment,
                it.confirmPaymentAbove,
            )
        }.launchIn(viewModelScope)
    }

    fun setConfirmPaymentAlways(v: Boolean) {
        _uiState.value = _uiState.value?.copy(alwaysConfirmPayments = v)
        save()
    }

    fun setConfirmPaymentAbove(v: Float) {
        _uiState.value = _uiState.value?.copy(confirmPaymentsAbove = v)
    }


    fun save() {
        val current = _uiState.value ?: return
        val paymentsSettingsModel = PaymentSettings(
            alwaysConfirmPayment = current.alwaysConfirmPayments,
            confirmPaymentAbove = current.confirmPaymentsAbove
        )
        viewModelScope.launch { settingsRepository.setPaymentSettings(paymentsSettingsModel) }
    }
}
