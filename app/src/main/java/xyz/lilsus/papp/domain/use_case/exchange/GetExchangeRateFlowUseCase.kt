package xyz.lilsus.papp.domain.use_case.exchange

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.exchange.ExchangeRate
import xyz.lilsus.papp.domain.model.exchange.ExchangeRateError
import xyz.lilsus.papp.domain.repository.ExchangeRateRepository
import java.util.Currency

/**
 * Emits the BTC->fiat exchange rate on a fixed polling interval.
 *
 * Design goals:
 * - Shared polling per currency per use-case instance (multiple collectors share the same upstream).
 * - No polling starts until there is at least one collector (lazy sharing).
 * - On errors, it skips emission and tries again on the next tick.
 */
class GetExchangeRateFlowUseCase(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val applicationScope: CoroutineScope,
    private val pollingIntervalMs: Long = 30_000L,
) {
    private val cache = mutableMapOf<Currency, Flow<ExchangeRate>>()

    @Synchronized
    operator fun invoke(vsCurrency: Currency): Flow<ExchangeRate> {
        // Return cached shared flow if present to ensure one polling loop per currency
        cache[vsCurrency]?.let { return it }

        val polling = flow {
            while (true) {
                try {
                    when (val res: Resource<ExchangeRate, ExchangeRateError> =
                        exchangeRateRepository.getExchangeRate(vsCurrency)) {
                        is Resource.Success -> emit(res.data)
                        is Resource.Error -> {
                            // ignore and try again on the next tick
                        }

                        is Resource.Loading -> {
                            // ignore loading in polling; repository should be synchronous per call
                        }
                    }

                } catch (_: Throwable) {
                    // Repository should not throw, even if it does its fine to just
                    // keep trying.
                }
                delay(pollingIntervalMs)
            }
        }.shareIn(
            scope = applicationScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            replay = 1,
        )

        cache[vsCurrency] = polling
        return polling
    }
}
