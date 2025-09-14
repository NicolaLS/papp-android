package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.StateFlow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.WalletResource
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.map
import xyz.lilsus.papp.domain.repository.WalletRepository

class ProbeFeeUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
) {
    suspend operator fun invoke(
        invoice: Invoice.Bolt11
    ): WalletResource<Long> {
        val repository = repositoryFlow.value
            ?: return Resource.Error(WalletRepositoryError.NoWalletConnected)

        val result = repository.probeBolt11PaymentFee(invoice)
            .map { it to repository.walletType }
        return result
    }
}
