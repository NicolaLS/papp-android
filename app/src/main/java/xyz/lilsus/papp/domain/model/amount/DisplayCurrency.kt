package xyz.lilsus.papp.domain.model.amount

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import xyz.lilsus.papp.domain.model.SatoshiAmount
import xyz.lilsus.papp.domain.model.exchange.ExchangeRate

/**
 * A lightweight description of how amounts should be displayed for the current app settings.
 *
 * Previously this class carried no formatting logic. We now add a small, focused
 * formatting helper that converts from a SatoshiAmount into the selected display
 * unit and formats it with the pre-configured NumberFormat. No exchange fetching
 * happens here; for fiat, callers must supply the ExchangeRate.
 *
 * Important decisions:
 * - The app's current Locale is used for ALL units, including BTC and SAT.
 * - We do not attempt to find a locale "for" a specific currency.
 */
sealed class DisplayCurrency(open val formatter: NumberFormat) {
    data class Sat(override val formatter: NumberFormat) : DisplayCurrency(formatter)
    data class Btc(override val formatter: NumberFormat) : DisplayCurrency(formatter)
    data class Fiat(val isoCode: String, override val formatter: NumberFormat) : DisplayCurrency(formatter)

    /**
     * Formats the given satoshi amount according to the display currency.
     * - SAT: integer with grouping, suffixed with " sat".
     * - BTC: sats converted to BTC (1 BTC = 100_000_000 sats), up to 8 decimals, suffixed with " BTC".
     * - Fiat: requires an ExchangeRate (price per 1 BTC); sats -> BTC -> fiat, formatted as currency.
     */
    fun format(amount: SatoshiAmount, exchangeRate: ExchangeRate? = null): String = when (this) {
        is Sat -> formatter.format(amount.value) + " sat"
        is Btc -> {
            val btc = BigDecimal(amount.value).divide(SATS_IN_BTC, 8, RoundingMode.HALF_UP)
            // Strip trailing zeros for cleaner output but keep at most 8 decimals.
            val display = btc.setScale(8, RoundingMode.DOWN).stripTrailingZeros()
            formatter.format(display) + " BTC"
        }
        is Fiat -> {
            val rate = requireNotNull(exchangeRate) { "ExchangeRate is required to format fiat amounts" }
            // price is fiat per 1 BTC
            val btc = BigDecimal(amount.value).divide(SATS_IN_BTC, 8, RoundingMode.HALF_UP)
            val fiat = btc.multiply(BigDecimal.valueOf(rate.price))
            formatter.format(fiat)
        }
    }

    companion object {
        private val SATS_IN_BTC = BigDecimal(100_000_000L)
    }
}
