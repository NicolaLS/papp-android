package xyz.lilsus.papp.domain.use_case.settings

import kotlinx.coroutines.flow.first
import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.repository.SettingsRepository

class GetPaymentSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): PaymentSettings {
        val paymentSettingsModel = settingsRepository.paymentSettings.first()
        return paymentSettingsModel
    }
}
