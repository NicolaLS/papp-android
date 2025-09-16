package xyz.lilsus.papp.domain.model.amount

import xyz.lilsus.papp.domain.model.exchange.ExchangeRate
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

sealed interface Formatter {
    fun format(satoshiAmount: SatoshiAmount): String
}

class BitcoinFormatter(locale: Locale) : Formatter {
    private val nf = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 8
    }

    override fun format(satoshiAmount: SatoshiAmount): String {
        val btcValue = BigDecimal(satoshiAmount.value).divide(SATS_IN_BTC, 8, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        return nf.format(btcValue) + " BTC"
    }

    companion object {
        private val SATS_IN_BTC = BigDecimal(100_000_000L)
    }
}

class SatoshiFormatter(locale: Locale) : Formatter {
    private val nf = NumberFormat.getIntegerInstance(locale)

    override fun format(satoshiAmount: SatoshiAmount): String =
        nf.format(satoshiAmount.value) + " sat"


}

class FiatFormatter(
    locale: Locale,
    isoCurrency: Currency,
    private val rate: ExchangeRate
) : Formatter {
    private val nf = NumberFormat.getCurrencyInstance(locale).apply { currency = isoCurrency }

    override fun format(satoshiAmount: SatoshiAmount): String {
        // price is fiat per 1 BTC
        val btc = BigDecimal(satoshiAmount.value).divide(SATS_IN_BTC, 8, RoundingMode.HALF_UP)
        val fiatValue = btc.multiply(BigDecimal.valueOf(rate.price))
        return nf.format(fiatValue)
    }

    companion object {
        private val SATS_IN_BTC = BigDecimal(100_000_000L)
    }
}