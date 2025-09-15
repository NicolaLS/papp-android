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
import xyz.lilsus.papp.domain.model.mapError
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.presentation.model.PaymentData
import xyz.lilsus.papp.presentation.model.PaymentError
import xyz.lilsus.papp.presentation.model.amount.UiAmount

class PayInvoiceUseCase(private val repositoryFlow: StateFlow<WalletRepository?>) {
    operator fun invoke(invoice: Invoice.Bolt11): Flow<Resource<PaymentData, PaymentError>> =
        flow {
            emit(Resource.Loading)
            val repository = repositoryFlow.value
            if (repository == null) {
                emit(
                    Resource.Error(
                        PaymentError.fromDomainWalletError(
                            WalletRepositoryError.NoWalletConnected,
                            WalletTypeEntry.NOT_SET
                        )
                    )
                )
                return@flow
            }
            val result = repository.payBolt11Invoice(invoice)
                .map {
                    val wt = repository.walletType
                    when (it) {
                        SendPaymentData.AlreadyPaid -> PaymentData.AlreadyPaid(wt)
                        SendPaymentData.Pending -> PaymentData.Pending(wt)
                        is SendPaymentData.Success -> PaymentData.Paid(
                            // TODO: Now we've got the type but we hardcode it to Sats still
                            // nextup create a use case for this using locale and exchange rates.
                            amountPaid = UiAmount.Sats(it.amountPaid.value),
                            feePaid = UiAmount.Sats(it.feePaid.value),
                            walletType = wt
                        )
                    }
                }
                .mapError { PaymentError.fromDomainWalletError(it, repository.walletType) }
            emit(result)
        }
}
