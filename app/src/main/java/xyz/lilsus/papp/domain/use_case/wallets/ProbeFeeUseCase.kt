package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.StateFlow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.common.map
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository

class ProbeFeeUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
) {
    suspend operator fun invoke(
        invoice: Invoice.Bolt11
    ): Resource<Pair<Long, WalletTypeEntry>> {
        val repository = repositoryFlow.value
            ?: return Resource.Error(WalletRepositoryError.NoWalletConnected)

        val result = repository.probeBolt11PaymentFee(invoice.bolt11)
            .map { it to repository.walletType }
        return result
    }
}
