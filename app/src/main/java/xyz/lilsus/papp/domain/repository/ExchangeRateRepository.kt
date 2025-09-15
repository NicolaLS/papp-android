package xyz.lilsus.papp.domain.repository

import java.util.Currency
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.exchange.ExchangeRate
import xyz.lilsus.papp.domain.model.exchange.ExchangeRateError

interface ExchangeRateRepository {
    /**
     * Fetches the current price of Bitcoin against the fiat [vsCurrency].
     * Example: USD -> price of 1 BTC in USD.
     */
    suspend fun getExchangeRate(
        vsCurrency: Currency
    ): Resource<ExchangeRate, ExchangeRateError>
}
