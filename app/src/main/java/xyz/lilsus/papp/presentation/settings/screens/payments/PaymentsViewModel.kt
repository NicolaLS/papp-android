package xyz.lilsus.papp.presentation.settings.screens.payments

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.use_case.settings.GetPaymentSettingsUseCase
import xyz.lilsus.papp.domain.use_case.settings.SavePaymentSettingsUseCase


data class PaymentsUIState(
    val alwaysConfirmPayments: Boolean,
    val confirmPaymentsAbove: Float
)

class PaymentsViewModel(
    private val getPaymentSettings: GetPaymentSettingsUseCase,
    private val savePaymentSettings: SavePaymentSettingsUseCase,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val settingsRepository = application.appDependencies.settingsRepository
                PaymentsViewModel(
                    GetPaymentSettingsUseCase(settingsRepository),
                    SavePaymentSettingsUseCase(settingsRepository)
                )
            }
        }
    }

    private val _uiState = mutableStateOf<PaymentsUIState?>(null)
    val uiState: State<PaymentsUIState?> = _uiState

    init {
        viewModelScope.launch {
            getPaymentSettings().collect { resource ->
                _uiState.value = when (resource) {
                    is Resource.Error<PaymentSettings> -> null
                    is Resource.Loading<PaymentSettings> -> null
                    is Resource.Success<PaymentSettings> -> PaymentsUIState(
                        resource.data.alwaysConfirmPayment,
                        resource.data.confirmPaymentAbove,
                    )
                }
            }
        }
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
        viewModelScope.launch {
            savePaymentSettings(paymentsSettingsModel)
        }
    }
}
