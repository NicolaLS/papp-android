package xyz.lilsus.papp.domain.model.amount

import java.util.Currency

sealed class DisplayCurrency {
    object Satoshi : DisplayCurrency()
    object Bitcoin : DisplayCurrency()
    data class Fiat(val iso4217Currency: Currency) : DisplayCurrency()

    fun getTag(): String = when (this) {
        Satoshi -> "SAT"
        Bitcoin -> "BTC"
        is Fiat -> this.iso4217Currency.currencyCode
    }
}