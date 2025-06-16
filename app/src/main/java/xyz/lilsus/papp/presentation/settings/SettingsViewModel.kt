package xyz.lilsus.papp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import xyz.lilsus.papp.data.repository.WalletRepositoryImpl
import xyz.lilsus.papp.proto.wallet_config.BlinkWalletConfig
import xyz.lilsus.papp.proto.wallet_config.WalletConfig
import xyz.lilsus.papp.proto.wallet_config.WalletType

class SettingsViewModel(
    private val walletRepository: WalletRepositoryImpl
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey

    private val _walletId = MutableStateFlow("")
    val walletId: StateFlow<String> = _walletId

    private val _statusMessage = MutableStateFlow("Not connected")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        viewModelScope.launch {
            walletRepository.walletConfigFlow.collect { config ->
                if (config.activeWalletType == WalletType.WALLET_TYPE_BLINK) {
                    val blink = config.blinkWallet
                    // Only update if values differ (avoid overwriting in-progress edits)
                    if (_apiKey.value != blink.apiKey) _apiKey.value = blink.apiKey
                    if (_walletId.value != blink.walletId) _walletId.value = blink.walletId
                    _statusMessage.value = "Connected"
                    _isConnected.value = true
                } else {
                    if (_apiKey.value.isNotEmpty()) _apiKey.value = ""
                    if (_walletId.value.isNotEmpty()) _walletId.value = ""
                    _statusMessage.value = "Not connected"
                    _isConnected.value = false
                }
            }
        }
    }


    fun onApiKeyChange(newKey: String) {
        _apiKey.value = newKey
    }

    fun onWalletIdChange(newId: String) {
        _walletId.value = newId
    }

    fun connectBlinkWallet() {
        val config = WalletConfig.newBuilder()
            .setActiveWalletType(WalletType.WALLET_TYPE_BLINK)
            .setBlinkWallet(
                BlinkWalletConfig.newBuilder()
                    .setApiKey(_apiKey.value)
                    .setWalletId(_walletId.value)
                    .build()
            )
            .build()

        viewModelScope.launch {
            try {
                walletRepository.updateWalletConfig(config)
                _statusMessage.value = "Connected"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun disconnectWallet() {
        val config = WalletConfig.getDefaultInstance()  // resets to no wallet configured
        viewModelScope.launch {
            try {
                walletRepository.updateWalletConfig(config)
                _statusMessage.value = "Not connected"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }
}
