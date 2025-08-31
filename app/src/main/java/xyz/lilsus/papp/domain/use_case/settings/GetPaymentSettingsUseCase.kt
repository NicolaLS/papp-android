package xyz.lilsus.papp.domain.use_case.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.settings.PaymentSettings
import xyz.lilsus.papp.domain.repository.SettingsRepository

class GetPaymentSettingsUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<Resource<PaymentSettings>> =
        flow {
            emit(Resource.Loading())
            settingsRepository.paymentSettings.collect { settings ->
                emit(
                    Resource.Success(
                        PaymentSettings(
                            settings.alwaysConfirmPayment,
                            settings.confirmPaymentAbove,
                        )
                    )
                )
            }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "Unknown Error"))
        }
}

