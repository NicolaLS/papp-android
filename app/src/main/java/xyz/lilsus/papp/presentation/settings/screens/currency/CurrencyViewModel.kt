package xyz.lilsus.papp.presentation.settings.screens.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.repository.SettingsRepository


data class CurrencyScreenState(
    val selectedCurrencyTag: String = "",
    val searchQuery: String = ""
)

class CurrencyViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val settingsRepository = application.appDependencies.settingsRepository
                CurrencyViewModel(
                    settingsRepository
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(CurrencyScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val selected = settingsRepository.currency.first()
            _uiState.value = CurrencyScreenState(
                selectedCurrencyTag = selected.getTag()
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCurrencySelected(tag: String) {
        viewModelScope.launch {
            settingsRepository.setCurrency(tag)
            _uiState.update { it.copy(selectedCurrencyTag = tag) }
        }
    }

}
