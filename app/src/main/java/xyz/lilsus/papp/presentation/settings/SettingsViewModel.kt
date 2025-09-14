package xyz.lilsus.papp.presentation.settings

import androidx.appcompat.app.AppCompatDelegate
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
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.domain.use_case.wallets.config.GetActiveWalletUseCase
import java.util.Locale

class SettingsViewModel(
    getActiveWallet: GetActiveWalletUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val walletConfigRepository = application.appDependencies.walletConfigRepository
                val settingsRepository = application.appDependencies.settingsRepository
                SettingsViewModel(
                    GetActiveWalletUseCase(walletConfigRepository),
                    settingsRepository
                )
            }
        }
    }

    val activeCurrency: StateFlow<String> = settingsRepository.currency
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "SAT"
        )

    /**
     * Gets the active language tag directly from the AppCompatDelegate.
     * This is a property with a custom getter, so it is re-evaluated every time it's accessed,
     * ensuring the UI always gets the latest value on recomposition.
     */
    val activeLanguageTag: String
        get() {
            val currentLocaleTag = AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
                ?: Locale.getDefault().toLanguageTag()

            val primaryLanguage = currentLocaleTag.split("-").first()
            return primaryLanguage
        }

    val activeWalletSubtitle: StateFlow<String?> = getActiveWallet()
        .map { resource ->
            when (resource) {
                is Resource.Success -> resource.data?.alias ?: "No active wallet"
                else -> null
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )
}
