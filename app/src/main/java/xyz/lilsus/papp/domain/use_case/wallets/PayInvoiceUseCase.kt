package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.WalletResource
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.map
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.use_case.CreateDisplayAmountUseCase
import xyz.lilsus.papp.presentation.model.UiSendPaymentData
import xyz.lilsus.papp.presentation.model.format

class PayInvoiceUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
    private val createDisplayAmount: CreateDisplayAmountUseCase,
) {
    operator fun invoke(invoice: Invoice.Bolt11): Flow<WalletResource<UiSendPaymentData>> =
        flow {
            emit(Resource.Loading)
            val repository = repositoryFlow.value
            if (repository == null) {
                emit(Resource.Error(WalletRepositoryError.NoWalletConnected))
                return@flow
            }
            val result = repository.payBolt11Invoice(invoice)
                .map { domain ->
                    val ui = when (domain) {
                        SendPaymentData.AlreadyPaid -> UiSendPaymentData.AlreadyPaid
                        SendPaymentData.Pending -> UiSendPaymentData.Pending
                        is SendPaymentData.Success -> {
                            val amount = createDisplayAmount(domain.amountPaid).format()
                            val fee = createDisplayAmount(domain.feePaid).format()
                            UiSendPaymentData.Success(amountPaidFormatted = amount, feePaidFormatted = fee)
                        }
                    }
                    ui to repository.walletType
                }
            emit(result)
        }
}
