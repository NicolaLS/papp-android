package xyz.lilsus.papp.domain.use_case.exchange

import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.exchange.ExchangeRate
import xyz.lilsus.papp.domain.model.exchange.ExchangeRateError
import xyz.lilsus.papp.domain.repository.ExchangeRateRepository
import java.util.Currency

/**
 * Use case that fetches and caches the BTC->fiat exchange rate for a short time window
 * to avoid repeated network calls within the same user workflow (e.g. probe -> pay result).
 */
class GetExchangeRateUseCase(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val ttlMillis: Long = 60_000L, // 1 minute default TTL
) {
    private data class CacheEntry(val rate: ExchangeRate, val timestamp: Long)

    private val cache: MutableMap<String, CacheEntry> = LinkedHashMap()

    suspend operator fun invoke(vsCurrency: Currency): Resource<ExchangeRate, ExchangeRateError> {
        val key = vsCurrency.currencyCode
        val now = System.currentTimeMillis()
        val cached = cache[key]
        if (cached != null && (now - cached.timestamp) < ttlMillis) {
            return Resource.Success(cached.rate)
        }

        return when (val fetched = exchangeRateRepository.getExchangeRate(vsCurrency)) {
            is Resource.Success -> {
                cache[key] = CacheEntry(fetched.data, now)
                fetched
            }
            is Resource.Error -> fetched
            is Resource.Loading -> fetched
        }
    }
}
