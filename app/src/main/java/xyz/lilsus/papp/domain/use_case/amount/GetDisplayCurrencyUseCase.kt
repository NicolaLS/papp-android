package xyz.lilsus.papp.domain.use_case.amount

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import xyz.lilsus.papp.domain.android.LocaleProvider
import xyz.lilsus.papp.domain.model.amount.DisplayCurrency
import xyz.lilsus.papp.domain.repository.SettingsRepository

/**
 * Emits a DisplayCurrency whenever the selected currency or the app locale changes.
 *
 * Design notes:
 * - Always uses the app's Locale (LocaleProvider) for all units, including BTC and SAT.
 * - No formatter caching: these objects are only created on changes and are cheap enough.
 */
class GetDisplayCurrencyUseCase(
    private val settingsRepository: SettingsRepository,
    private val localeProvider: LocaleProvider,
) {

    operator fun invoke(): Flow<DisplayCurrency> {
        // Combine currency setting changes with locale changes to re-create display config
        return combine(
            settingsRepository.currency.map { it.ifEmpty { DEFAULT_CURRENCY_TAG } },
            localeProvider.changes,
        ) { currencyTag, locale ->
            toDisplayCurrency(currencyTag, locale)
        }
    }

    private fun toDisplayCurrency(tag: String, locale: Locale): DisplayCurrency {
        return when (tag.uppercase(Locale.ROOT)) {
            "SAT" -> DisplayCurrency.Sat(
                NumberFormat.getIntegerInstance(locale)
            )
            "BTC" -> DisplayCurrency.Btc(
                NumberFormat.getNumberInstance(locale).apply {
                    minimumFractionDigits = 0
                    maximumFractionDigits = 8
                }
            )
            else -> {
                // Treat any other tag as fiat ISO 4217.
                val code = try {
                    Currency.getInstance(tag.uppercase(Locale.ROOT)).currencyCode
                } catch (_: IllegalArgumentException) {
                    DEFAULT_FALLBACK_FIAT
                }
                val formatter = NumberFormat.getCurrencyInstance(locale).apply {
                    currency = Currency.getInstance(code)
                }
                DisplayCurrency.Fiat(code, formatter)
            }
        }
    }

    companion object {
        private const val DEFAULT_CURRENCY_TAG = "SAT"
        private const val DEFAULT_FALLBACK_FIAT = "USD"
    }
}
