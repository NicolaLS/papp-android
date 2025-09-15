package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.StateFlow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.model.map
import xyz.lilsus.papp.domain.model.mapError
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.presentation.model.PaymentError
import xyz.lilsus.papp.presentation.model.amount.UiAmount

class ProbeFeeUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
) {
    suspend operator fun invoke(
        invoice: Invoice.Bolt11
    ): Resource<UiAmount, PaymentError> {
        val repository = repositoryFlow.value
            ?: return Resource.Error(
                PaymentError.fromDomainWalletError(
                    WalletRepositoryError.NoWalletConnected,
                    WalletTypeEntry.NOT_SET
                )
            )

        val result = repository.probeBolt11PaymentFee(invoice)
            .map { UiAmount.Sats(it.value) }
            .mapError { PaymentError.fromDomainWalletError(it, repository.walletType) }
        return result
    }
}
