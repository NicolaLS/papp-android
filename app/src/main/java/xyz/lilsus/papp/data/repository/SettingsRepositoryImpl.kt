package xyz.lilsus.papp.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import xyz.lilsus.papp.common.Constants
import xyz.lilsus.papp.domain.model.amount.DisplayCurrency
import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.proto.settings.Payments
import xyz.lilsus.papp.proto.settings.SettingsStore
import java.util.Currency
import java.util.Locale

class SettingsRepositoryImpl(
    private val dataStore: DataStore<SettingsStore>,
) : SettingsRepository {
    override val paymentSettings =
        dataStore.data.distinctUntilChangedBy { it.paymentSettings }.map {
            PaymentSettings(
                it.paymentSettings.alwaysConfirmPayment,
                it.paymentSettings.confirmPaymentAbove
            )
        }

    override suspend fun setPaymentSettings(paymentSettings: PaymentSettings) {
        dataStore.updateData { current ->
            val builder = current.toBuilder()

            val paymentSettingsBuilder = Payments.newBuilder().apply {
                alwaysConfirmPayment = paymentSettings.alwaysConfirmPayment
                confirmPaymentAbove = paymentSettings.confirmPaymentAbove
            }

            builder.setPaymentSettings(paymentSettingsBuilder)
            builder.build()
        }
    }

    override val currency = dataStore.data.map {
        val tag = it.currency.uppercase(Locale.ROOT)
        when (tag) {
            "SAT" -> DisplayCurrency.Satoshi
            "BTC" -> DisplayCurrency.Bitcoin
            else -> {
                // ISO 4217.
                val c = try {
                    Currency.getInstance(tag)
                } catch (_: IllegalArgumentException) {
                    Currency.getInstance(Constants.DEFAULT_FALLBACK_FIAT)
                }
                DisplayCurrency.Fiat(c)
            }
        }
    }

    override suspend fun setCurrency(currency: String) {
        dataStore.updateData { current ->
            current.toBuilder().setCurrency(currency).build()
        }
    }
}
