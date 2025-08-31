package xyz.lilsus.papp.domain.use_case.settings

import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.repository.SettingsRepository

class SavePaymentSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(paymentSettings: PaymentSettings) {
        settingsRepository.setPaymentSettings(paymentSettings)
    }
}

