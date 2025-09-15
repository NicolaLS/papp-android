package xyz.lilsus.papp.domain.use_case

import kotlinx.coroutines.flow.first
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.presentation.model.DisplayAmount
import xyz.lilsus.papp.presentation.model.DisplayCurrency

class CreateDisplayAmountUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(satoshis: Long): DisplayAmount {
        val currencyTag = settingsRepository.currency.first().ifEmpty { "SAT" }

        val displayCurrency = when (currencyTag.uppercase()) {
            "SAT" -> DisplayCurrency.SAT
            "BTC" -> DisplayCurrency.BTC
            else -> DisplayCurrency.Fiat(currencyTag)
        }

        return DisplayAmount(
            satoshis = satoshis,
            currency = displayCurrency
        )
    }
}
