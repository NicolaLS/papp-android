package xyz.lilsus.papp.common

import java.util.Currency

object Constants {
    // Reasonable max. value for the "confirm amount above" slider.
    const val DEFAULT_ALWAYS_CONFIRM_PAYMENT = false
    const val DEFAULT_MAX_ABOVE = 100_000f

    // Reasonable default "confirm amount above" value.
    const val DEFAULT_CONFIRM_ABOVE = 20_000f
    const val DEFAULT_CURRENCY_CODE = "SAT"
    const val DEFAULT_FALLBACK_FIAT = "USD"

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

    // Stable list of supported currency tags (codes) including Bitcoin units.
    val SUPPORTED_CURRENCY_CODES: List<String> = buildList {
        add("SAT")
        add("BTC")
        addAll(SUPPORTED_FIAT_CURRENCIES.map { it.currencyCode })
    }
}
