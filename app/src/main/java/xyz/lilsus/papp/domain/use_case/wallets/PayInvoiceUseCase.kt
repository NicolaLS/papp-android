package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository

class PayInvoiceUseCase(private val repository: WalletRepository?) {
    operator fun invoke(invoice: Bolt11Invoice): Flow<Resource<Pair<SendPaymentData, WalletTypeEntry>>> =
        flow {
            emit(Resource.Loading())
            if (repository != null) {
                try {
                    repository.payBolt11Invoice(invoice).fold(
                        onSuccess = { emit(Resource.Success(Pair(it, repository.walletType))) },
                        onFailure = { emit(Resource.Error(it.message ?: "Something went wrong")) },
                    )

                } catch (e: Exception) {
                    emit(Resource.Error(message = "Something went wrong...\n${e.message}"))
                }
            } else {
                emit(Resource.Error(message = "No active wallet configured"))
            }
        }
}