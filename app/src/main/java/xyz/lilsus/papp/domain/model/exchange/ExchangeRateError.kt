package xyz.lilsus.papp.domain.model.exchange

sealed interface ExchangeRateError {
    data class Http(val code: Int) : ExchangeRateError
    object Network : ExchangeRateError
    object Parse : ExchangeRateError
    data class Unknown(val message: String? = null) : ExchangeRateError
}
