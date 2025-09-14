package xyz.lilsus.papp.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.proto.settings.Payments
import xyz.lilsus.papp.proto.settings.SettingsStore

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

    override val currency = dataStore.data.map { it.currency }

    override suspend fun setCurrency(currency: String) {
        dataStore.updateData { current ->
            current.toBuilder().setCurrency(currency).build()
        }
    }
}
