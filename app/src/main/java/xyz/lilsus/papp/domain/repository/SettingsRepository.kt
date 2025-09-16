package xyz.lilsus.papp.domain.repository

import kotlinx.coroutines.flow.Flow
import xyz.lilsus.papp.domain.model.amount.DisplayCurrency
import xyz.lilsus.papp.domain.model.settings.PaymentSettings

interface SettingsRepository {
    val paymentSettings: Flow<PaymentSettings>
    suspend fun setPaymentSettings(paymentSettings: PaymentSettings): Unit

    val currency: Flow<DisplayCurrency>
    suspend fun setCurrency(currency: String): Unit
}
