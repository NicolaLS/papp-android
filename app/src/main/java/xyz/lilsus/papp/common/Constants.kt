package xyz.lilsus.papp.common

import xyz.lilsus.papp.presentation.settings.components.SettingOption

object Constants {
    // Reasonable max. value for the "confirm amount above" slider.
    const val DEFAULT_ALWAYS_CONFIRM_PAYMENT = false
    const val DEFAULT_MAX_ABOVE = 100_000f

    // Reasonable default "confirm amount above" value.
    const val DEFAULT_CONFIRM_ABOVE = 20_000f

    val SUPPORTED_CURRENCIES = listOf(
        SettingOption("Satoshi", "SAT"),
        SettingOption("Bitcoin", "BTC"),
        SettingOption("US Dollar", "USD"),
        SettingOption("Euro", "EUR"),
        SettingOption("British Pound", "GBP"),
        SettingOption("Canadian Dollar", "CAD"),
        SettingOption("Australian Dollar", "AUD"),
        SettingOption("Swiss Franc", "CHF"),
        SettingOption("Japanese Yen", "JPY"),
        SettingOption("Chinese Yuan", "CNY"),
        SettingOption("Indian Rupee", "INR")
    )
}
