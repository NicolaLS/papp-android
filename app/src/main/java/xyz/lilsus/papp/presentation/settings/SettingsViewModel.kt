package xyz.lilsus.papp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.use_case.wallets.config.GetActiveWalletUseCase

class SettingsViewModel(
    getActiveWallet: GetActiveWalletUseCase,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val walletConfigRepository = application.appDependencies.walletConfigRepository
                SettingsViewModel(
                    GetActiveWalletUseCase(walletConfigRepository),
                )
            }
        }
    }

    val activeWalletSubtitle: StateFlow<String?> = getActiveWallet()
        .map { resource ->
            when (resource) {
                is Resource.Loading -> null
                is Resource.Success -> resource.data?.alias ?: "No active wallet"
                is Resource.Error -> null
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
}
