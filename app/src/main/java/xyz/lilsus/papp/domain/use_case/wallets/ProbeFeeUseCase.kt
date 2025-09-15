package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.StateFlow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.SatoshiAmount
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.presentation.model.PaymentError

class ProbeFeeUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
) {
    suspend operator fun invoke(
        invoice: Invoice.Bolt11
    ): Resource<SatoshiAmount, PaymentError> {
        val repository = repositoryFlow.value
            ?: return Resource.Error(
                PaymentError.fromDomainWalletError(
                    WalletRepositoryError.NoWalletConnected,
                    WalletTypeEntry.NOT_SET
                )
            )

        return when (val result = repository.probeBolt11PaymentFee(invoice)) {
            is Resource.Success -> Resource.Success(SatoshiAmount(result.data.value))
            is Resource.Error -> Resource.Error(
                PaymentError.fromDomainWalletError(result.error, repository.walletType)
            )
            is Resource.Loading -> Resource.Loading
        }
    }
}
