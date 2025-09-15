package xyz.lilsus.papp.presentation.model.amount

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * UI-facing amount types that are currency-aware and can be formatted for display.
 *
 * Important: The numeric values stored here must already represent the final
 * value for the target currency/unit. The UI will only format, not convert.
 */
// TODO: Maybe pass number formatter to UiAmount from the use case to avoid business logic here.
sealed class UiAmount {
    /**
     * Format the amount for display. No currency conversion happens here.
     * Implementations will choose an appropriate Locale automatically.
     */
    abstract fun format(): String

    /** Satoshi amount (1 BTC = 100_000_000 sats). */
    data class Sats(val sats: Long) : UiAmount() {
        override fun format(): String {
            val nf = NumberFormat.getIntegerInstance(Locale.getDefault())
            return nf.format(sats) + " sat"
        }
    }

    /** Bitcoin amount represented as BTC with up to 8 fraction digits. */
    data class Bitcoin(val btc: BigDecimal) : UiAmount() {
        override fun format(): String {
            val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                minimumFractionDigits = 0
                maximumFractionDigits = 8
            }
            // Strip trailing zeros for cleaner output, but cap at 8 decimals.
            val scaled = btc.setScale(8, RoundingMode.DOWN).stripTrailingZeros()
            return nf.format(scaled) + " BTC"
        }
    }

    /**
     * Fiat amount with ISO 4217 currency code. The amount must already be converted.
     */
    data class Fiat(val amount: BigDecimal, val currencyCode: String) : UiAmount() {
        override fun format(): String {
            val currency = Currency.getInstance(currencyCode)
            val locale = localeForCurrency(currency) ?: Locale.getDefault()
            val nf = NumberFormat.getCurrencyInstance(locale)
            nf.currency = currency
            return nf.format(amount)
        }
    }
}

private fun localeForCurrency(currency: Currency): Locale? {
    // Try to find a locale whose default currency matches the provided currency.
    return Locale.getAvailableLocales()
        .asSequence()
        .filter { it.country.isNotEmpty() }
        .firstOrNull { locale ->
            try {
                Currency.getInstance(locale) == currency
            } catch (_: Exception) {
                false
            }
        }
}
