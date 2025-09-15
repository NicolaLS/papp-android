package xyz.lilsus.papp.domain.use_case.amount

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency
import kotlinx.coroutines.flow.first
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.domain.use_case.exchange.GetExchangeRateUseCase
import xyz.lilsus.papp.presentation.model.amount.UiAmount

/**
 * Creates UI-facing amounts based on the user's selected currency setting.
 * - If setting is SAT: returns UiAmount.Sats
 * - If setting is BTC: returns UiAmount.Bitcoin
 * - If setting is fiat (ISO 4217): fetches exchange rate (cached) and returns UiAmount.Fiat
 *   converting sats to fiat using the current BTC price.
 *
 * Fallback strategy: If fetching exchange rate fails, we fall back to Satoshi display,
 * so the UI always has something to show and payments aren't blocked by price fetch issues.
 */
class CreateUiAmountUseCase(
    private val settingsRepository: SettingsRepository,
    private val getExchangeRate: GetExchangeRateUseCase,
) {
    companion object {
        private val SATS_IN_BTC = BigDecimal(100_000_000L)
    }

    suspend fun fromSats(sats: Long): UiAmount {
        val tag = settingsRepository.currency.first().ifEmpty { "SAT" }
        return fromSatsWithCurrencyTag(sats, tag)
    }

    suspend fun fromSatsWithCurrencyTag(sats: Long, tag: String): UiAmount {
        return when (tag.uppercase()) {
            "SAT" -> UiAmount.Sats(sats)
            "BTC" -> {
                val btc = BigDecimal(sats).divide(SATS_IN_BTC, 8, RoundingMode.HALF_UP)
                UiAmount.Bitcoin(btc)
            }
            else -> {
                try {
                    val currency = Currency.getInstance(tag.uppercase())
                    when (val res = getExchangeRate(currency)) {
                        is xyz.lilsus.papp.domain.model.Resource.Success -> {
                            // price is fiat per 1 BTC
                            val price = BigDecimal.valueOf(res.data.price)
                            val btc = BigDecimal(sats).divide(SATS_IN_BTC, 8, RoundingMode.HALF_UP)
                            val fiatAmount = btc.multiply(price)
                            UiAmount.Fiat(fiatAmount, currency.currencyCode)
                        }
                        is xyz.lilsus.papp.domain.model.Resource.Error -> UiAmount.Sats(sats)
                        is xyz.lilsus.papp.domain.model.Resource.Loading -> UiAmount.Sats(sats)
                    }
                } catch (_: IllegalArgumentException) {
                    // Unknown currency tag; fall back to sats
                    UiAmount.Sats(sats)
                }
            }
        }
    }
}
