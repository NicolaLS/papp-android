package xyz.lilsus.papp.domain.model.amount

import java.text.NumberFormat

/**
 * A lightweight description of how amounts should be displayed for the current app settings.
 *
 * This model purposely carries no concrete values or formatting logic. It simply
 * indicates the selected display unit (SAT, BTC, or a fiat ISO code) and provides
 * a pre-configured NumberFormat instance to be used by the UI layer.
 *
 * Important decisions:
 * - The app's current Locale is used for ALL units, including BTC and SAT.
 * - We do not attempt to find a locale "for" a specific currency.
 */
sealed class DisplayCurrency(open val formatter: NumberFormat) {
    data class Sat(override val formatter: NumberFormat) : DisplayCurrency(formatter)
    data class Btc(override val formatter: NumberFormat) : DisplayCurrency(formatter)
    data class Fiat(val isoCode: String, override val formatter: NumberFormat) : DisplayCurrency(formatter)
}
