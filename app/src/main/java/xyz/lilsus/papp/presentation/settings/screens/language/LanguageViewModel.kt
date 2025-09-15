package xyz.lilsus.papp.presentation.settings.screens.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.domain.android.LocaleProvider
import xyz.lilsus.papp.presentation.model.SettingOption

data class LanguageScreenState(
    val languages: List<SettingOption> = emptyList(),
    val selectedLanguageTag: String = "",
    val searchQuery: String = "",
    val currentLanguage: String = ""
)

class LanguageViewModel(
    private val localeProvider: LocaleProvider
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as PappApplication)
                val localeProvider = application.appDependencies.localeProvider
                LanguageViewModel(localeProvider)
            }
        }
    }

    private val _uiState = MutableStateFlow(LanguageScreenState())
    val uiState = _uiState.asStateFlow()

    private val supportedLanguages = mapOf(
        "en" to "English",
        "de" to "Deutsch",
        "es" to "Español",
    )

    init {
        // React to locale changes and update UI state accordingly
        viewModelScope.launch {
            localeProvider.changes.collect { locale ->
                val currentLocaleTag = locale.toLanguageTag()
                val primaryLanguage = currentLocaleTag.split("-").first()
                val selectedTag = if (supportedLanguages.containsKey(primaryLanguage)) {
                    primaryLanguage
                } else {
                    "en"
                }
                val languageOptions = supportedLanguages.map { (tag, nativeLanguageName) ->
                    SettingOption(nativeLanguageName, tag)
                }
                _uiState.value = LanguageScreenState(
                    languages = languageOptions,
                    selectedLanguageTag = selectedTag,
                    currentLanguage = supportedLanguages.getValue(selectedTag)
                )
            }
        }
    }

    // This will trigger localeProvider.changes and update the state automatically
    fun refresh() {
        localeProvider.refresh()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onLanguageSelected(tag: String) {
        // This will trigger localeProvider.changes and update the state automatically
        localeProvider.setAppLocales(tag)
    }

}
