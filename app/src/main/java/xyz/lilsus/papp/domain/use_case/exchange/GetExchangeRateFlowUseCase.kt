package xyz.lilsus.papp.domain.use_case.exchange

import java.util.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.exchange.ExchangeRate
import xyz.lilsus.papp.domain.model.exchange.ExchangeRateError
import xyz.lilsus.papp.domain.repository.ExchangeRateRepository

/**
 * Emits the BTC->fiat exchange rate on a fixed polling interval.
 *
 * - No internal caching beyond what the repository may implement.
 * - On errors, we simply skip emission and try again on the next tick.
 */
class GetExchangeRateFlowUseCase(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val pollingIntervalMs: Long = 30_000L,
) {
    operator fun invoke(vsCurrency: Currency): Flow<ExchangeRate> = flow {
        while (coroutineContext.isActive) {
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
            delay(pollingIntervalMs)
        }
    }
}
