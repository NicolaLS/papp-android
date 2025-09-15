package xyz.lilsus.papp.common

import xyz.lilsus.papp.presentation.model.SettingOption
import java.util.Currency

object Constants {
    // Reasonable max. value for the "confirm amount above" slider.
    const val DEFAULT_ALWAYS_CONFIRM_PAYMENT = false
    const val DEFAULT_MAX_ABOVE = 100_000f

    // Reasonable default "confirm amount above" value.
    const val DEFAULT_CONFIRM_ABOVE = 20_000f

    // Type-safe list of supported fiat currencies for the app (java.util.Currency)
    // Note: BTC and SAT are not ISO 4217 currencies, so they are handled separately in the
    // UI options list below.
    val SUPPORTED_FIAT_CURRENCIES: List<Currency> = listOf(
        Currency.getInstance("USD"),
        Currency.getInstance("EUR"),
        Currency.getInstance("GBP"),
        Currency.getInstance("CAD"),
        Currency.getInstance("AUD"),
        Currency.getInstance("CHF"),
        Currency.getInstance("JPY"),
        Currency.getInstance("CNY"),
        Currency.getInstance("INR"),
    )

    // UI options derived from the type-safe currency list plus Bitcoin units.
    // We keep tags as stable String codes for persistence and navigation, but the
    // underlying fiat options are sourced from java.util.Currency.
    val SUPPORTED_CURRENCIES: List<SettingOption> = buildList {
        // Bitcoin units (non-ISO)
        add(SettingOption("Satoshi", "SAT"))
        add(SettingOption("Bitcoin", "BTC"))

        // Fiat currencies (ISO 4217)
        addAll(SUPPORTED_FIAT_CURRENCIES.map { c ->
            // Use the localized display name where available
            SettingOption(c.displayName, c.currencyCode)
        })
    }
}
