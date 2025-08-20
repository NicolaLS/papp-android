package xyz.lilsus.papp.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.use_case.wallets.config.AddWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.GetActiveWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.GetAllWalletsUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.RemoveWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.SetActiveWalletUseCase

sealed class WalletOption {
    data class Wallet(val entry: WalletEntry) : WalletOption()
    object None : WalletOption()
}


class SettingsViewModel(
    getAllWallets: GetAllWalletsUseCase,
    getActiveWallet: GetActiveWalletUseCase,
    private val setActiveWallet: SetActiveWalletUseCase,
    private val removeWallet: RemoveWalletUseCase,
    private val addWallet: AddWalletUseCase
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val walletConfigRepository = application.appDependencies.walletConfigRepository
                SettingsViewModel(
                    GetAllWalletsUseCase(walletConfigRepository),
                    GetActiveWalletUseCase(walletConfigRepository),
                    SetActiveWalletUseCase(walletConfigRepository),
                    RemoveWalletUseCase(walletConfigRepository),
                    AddWalletUseCase(walletConfigRepository)
                )
            }
        }
    }

    val allWallets = getAllWallets().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading()
    )

    val activeWallet = getActiveWallet().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading()
    )

    val selectedWallet: StateFlow<WalletOption> = activeWallet.map { resource ->
        when (resource) {
            is Resource.Success -> {
                resource.data?.let { WalletOption.Wallet(it) } ?: WalletOption.None
            }

            else -> WalletOption.None
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WalletOption.None
    )

    var showWalletTypeModal by mutableStateOf(false)

    fun onWalletSelected(selection: WalletOption) {
        viewModelScope.launch {
            when (selection) {
                WalletOption.None -> setActiveWallet(null)
                is WalletOption.Wallet -> setActiveWallet(selection.entry.key)
            }
        }
    }

    fun onRemoveSelected() {
        when (val selection = selectedWallet.value) {
            WalletOption.None -> {}
            is WalletOption.Wallet -> {
                viewModelScope.launch {
                    removeWallet(selection.entry.key)
                }
            }
        }
    }

    fun onAddWalletClicked() {
        showWalletTypeModal = true
    }

    fun connectWallet(wallet: AddWalletEntry) {
        viewModelScope.launch {
            addWallet(wallet)
            showWalletTypeModal = false

        }
    }
}
