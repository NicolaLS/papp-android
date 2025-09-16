package xyz.lilsus.papp.domain.use_case.amount

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import xyz.lilsus.papp.domain.android.LocaleProvider
import xyz.lilsus.papp.domain.model.amount.BitcoinFormatter
import xyz.lilsus.papp.domain.model.amount.DisplayCurrency
import xyz.lilsus.papp.domain.model.amount.FiatFormatter
import xyz.lilsus.papp.domain.model.amount.Formatter
import xyz.lilsus.papp.domain.model.amount.SatoshiFormatter
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.domain.use_case.exchange.GetExchangeRateFlowUseCase
import java.util.Currency
import java.util.Locale

/**
 * Exposes a reactive [Flow] of [Formatter]s that updates when:
 *  - App/User locale changes
 *  - Display currency changes (SAT/BTC/Fiat)
 *  - Exchange rate updates (only when a fiat is selected)
 *
 * Design goals:
 *  - No exchange-rate polling unless a fiat currency is selected.
 *  - When fiat is selected, the exchange-rate polling is shared across all collectors
 *    via [GetExchangeRateFlowUseCase].
 */
class GetFormatterFlowUseCase(
    private val settingsRepository: SettingsRepository,
    private val localeProvider: LocaleProvider,
    private val getExchangeRateFlowUseCase: GetExchangeRateFlowUseCase,
) {

    operator fun invoke(): Flow<Formatter?> {
        // React to changes in selected currency first, then build the right downstream flow.
        return settingsRepository.currency.flatMapLatest { displayCurrency ->
            when (displayCurrency) {
                is DisplayCurrency.Satoshi -> localeProvider.changes.map { locale ->
                    SatoshiFormatter(locale) as Formatter?
                }

                is DisplayCurrency.Bitcoin -> localeProvider.changes.map { locale ->
                    BitcoinFormatter(locale) as Formatter?
                }

                is DisplayCurrency.Fiat -> fiatFormatterFlow(
                    localeFlow = localeProvider.changes,
                    isoCurrency = displayCurrency.iso4217Currency
                )
            }
        }
    }

    private fun fiatFormatterFlow(
        localeFlow: Flow<Locale>,
        isoCurrency: Currency,
    ): Flow<Formatter?> {
        // Start exchange-rate flow only when Fiat is selected. This function is only
        // called in that branch thanks to flatMapLatest above.
        val rateFlow = getExchangeRateFlowUseCase(isoCurrency)
        return combine(localeFlow, rateFlow) { locale, rate ->
            FiatFormatter(locale, isoCurrency, rate)
        }
    }
}
