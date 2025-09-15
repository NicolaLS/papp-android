package xyz.lilsus.papp.domain.android

import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

// This is useful for two reasons:
// 1) ViewModels that get/set locale don't depend on android specifics (androidx)
// 2) We get partial reactivity to locale changes. If user changes locale in settings
// and comes back to a payment result screen, formatting of the amounts is not stale.
// This is partial because if the user changes locale in app language system settings
// then this simple provider will be of no use. this could be done with a LocaleRepository
// that depends on app context, but that's overkill especially because I don't even use safed
// state in the views yet.
// NOTE:
// refresh() method is a bit weird, but it enables UI to force this to refresh on configuration change
// see Language.kt for example.
interface LocaleProvider {
    val current: Locale
    val changes: StateFlow<Locale>
    fun setAppLocales(languageTags: String)
    fun refresh()
}