package xyz.lilsus.papp.data.android

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import xyz.lilsus.papp.domain.android.LocaleProvider
import java.util.Locale

class SimpleLocaleProvider : LocaleProvider {
    private val _changes = MutableStateFlow(readCurrent())
    override val changes: StateFlow<Locale> = _changes
    override val current: Locale get() = _changes.value

    override fun setAppLocales(languageTags: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTags))
        _changes.value = readCurrent()
    }

    override fun refresh() {
        _changes.value = readCurrent()
    }

    private fun readCurrent(): Locale {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) locales[0] else Locale.getDefault()
    }
}