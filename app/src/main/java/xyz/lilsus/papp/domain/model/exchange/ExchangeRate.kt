package xyz.lilsus.papp.domain.model.exchange

import java.util.Currency

/**
 * Domain model representing the price of Bitcoin in a fiat currency.
 * Example: 1 BTC = 62_000.50 USD
 */
 data class ExchangeRate(
     val vsCurrency: Currency, // ISO 4217 currency, e.g., USD, EUR
     val price: Double // price per 1 BTC
 )
