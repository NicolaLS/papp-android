package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository

class PayInvoiceUseCase(private val repository: WalletRepository?) {
    operator fun invoke(invoice: Invoice): Flow<Resource<Pair<SendPaymentResult, WalletTypeEntry>>> =
        flow {
            emit(Resource.Loading())
            if (repository != null) {
                try {
                    val res = repository.payBolt11Invoice(invoice)
                    emit(Resource.Success(Pair(res, repository.walletType)))
                } catch (e: Exception) {
                    emit(Resource.Error(message = "Something went wrong...\n${e.message}"))
                }
            } else {
                emit(Resource.Error(message = "No active wallet configured"))
            }
        }
}