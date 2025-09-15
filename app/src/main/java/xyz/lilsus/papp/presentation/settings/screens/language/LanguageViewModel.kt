package xyz.lilsus.papp.presentation.settings.screens.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import xyz.lilsus.papp.presentation.model.SettingOption
import java.util.Locale

data class LanguageScreenState(
    val languages: List<SettingOption> = emptyList(),
    val selectedLanguageTag: String = "",
    val searchQuery: String = "",
    val currentLanguage: String = ""
)

class LanguageViewModel : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LanguageViewModel()
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

    fun refresh() {
        val currentLocaleTag = AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
            ?: Locale.getDefault().toLanguageTag()

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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onLanguageSelected(tag: String) {
        val appLocale = LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(appLocale)
        // After setting the locale, we need to reload the languages to get the new display names.
        refresh()
    }

}
