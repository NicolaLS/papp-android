package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.amount.SatoshiAmount
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.presentation.model.PaymentData
import xyz.lilsus.papp.presentation.model.PaymentError

class PayInvoiceUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
) {
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
            when (val result = repository.payBolt11Invoice(invoice)) {
                is Resource.Success -> {
                    val wt = repository.walletType
                    val data = when (val it = result.data) {
                        SendPaymentData.AlreadyPaid -> PaymentData.AlreadyPaid(wt)
                        SendPaymentData.Pending -> PaymentData.Pending(wt)
                        is SendPaymentData.Success -> PaymentData.Paid(
                            amountPaid = SatoshiAmount(it.amountPaid.value),
                            feePaid = SatoshiAmount(it.feePaid.value),
                            walletType = wt
                        )
                    }
                    emit(Resource.Success(data))
                }

                is Resource.Error -> emit(
                    Resource.Error(
                        PaymentError.fromDomainWalletError(result.error, repository.walletType)
                    )
                )

                is Resource.Loading -> emit(Resource.Loading)
            }
        }
}
