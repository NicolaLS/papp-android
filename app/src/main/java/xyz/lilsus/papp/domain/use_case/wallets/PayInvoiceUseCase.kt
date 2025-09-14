package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.model.map
import xyz.lilsus.papp.domain.repository.WalletRepository

class PayInvoiceUseCase(private val repositoryFlow: StateFlow<WalletRepository?>) {
    operator fun invoke(invoice: Invoice.Bolt11): Flow<Resource<Pair<SendPaymentData, WalletTypeEntry>>> =
        flow {
            emit(Resource.Loading)
            val repository = repositoryFlow.value
            if (repository == null) {
                emit(Resource.Error(WalletRepositoryError.NoWalletConnected))
                return@flow
            }
            val result = repository.payBolt11Invoice(invoice)
                .map { it to repository.walletType }
            emit(result)
        }
}
