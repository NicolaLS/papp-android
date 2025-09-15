package xyz.lilsus.papp.presentation.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

sealed interface DisplayCurrency {
    sealed interface Bitcoin : DisplayCurrency
    object SAT : Bitcoin
    object BTC : Bitcoin
    data class Fiat(val isoCode: String) : DisplayCurrency
}

data class DisplayAmount(
    val satoshis: Long,
    val currency: DisplayCurrency
)

// NOTE:
// - SAT and BTC use the device locale for number grouping and decimal separators.
// - For Fiat we try to use a locale associated with the ISO currency code (best-effort),
//   but we DO NOT convert sats to fiat here. This is strictly a placeholder until
//   an exchange-rate source is integrated.
fun DisplayAmount.format(): String {
    return when (this.currency) {
        is DisplayCurrency.SAT -> {
            // Simple satoshi formatting with thousands separators
            NumberFormat.getNumberInstance(Locale.getDefault()).format(this.satoshis) + " sats"
        }

        is DisplayCurrency.BTC -> {
            // Format as BTC with 8 decimal places using BigDecimal for precision
            val btcAmount = BigDecimal(this.satoshis).movePointLeft(8).setScale(8, RoundingMode.HALF_UP)
            val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                minimumFractionDigits = 8
                maximumFractionDigits = 8
            }
            nf.format(btcAmount) + " BTC"
        }

        is DisplayCurrency.Fiat -> {
            // Placeholder for now. This will be implemented in the next step.
            // For now, we'll just show the satoshi amount with the ISO code and
            // try to respect a locale typically used for that currency.
            val locale = localeForCurrency(this.currency.isoCode)
            NumberFormat.getNumberInstance(locale)
                .format(this.satoshis) + " ${this.currency.isoCode}"
        }
    }
}

private fun localeForCurrency(isoCode: String, fallback: Locale = Locale.getDefault()): Locale {
    // Try to find a locale that uses the given currency. If none is found, fall back.
    return try {
        val target = Currency.getInstance(isoCode.uppercase(Locale.ROOT))
        Locale.getAvailableLocales().firstOrNull { loc ->
            try {
                Currency.getInstance(loc) == target
            } catch (_: Exception) {
                false
            }
        } ?: fallback
    } catch (_: Exception) {
        fallback
    }
}
