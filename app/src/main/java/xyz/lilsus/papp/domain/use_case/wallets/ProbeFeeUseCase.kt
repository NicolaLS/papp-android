package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.StateFlow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository

class ProbeFeeUseCase(
    private val repositoryFlow: StateFlow<WalletRepository?>,
) {
    suspend operator fun invoke(
        bolt11Invoice: Invoice.Bolt11
    ): Resource<Pair<Long, WalletTypeEntry>> {
        val repo = repositoryFlow.value
            ?: return Resource.Error("No active wallet configured")

        return try {
            val result = repo.probeBolt11PaymentFee(bolt11Invoice.bolt11)

            result.fold(
                onSuccess = { Resource.Success(it to repo.walletType) },
                onFailure = { Resource.Error(it.message ?: "Something went wrong") }
            )
        } catch (e: Exception) {
            Resource.Error("Something went wrong...\n${e.message}")
        }
    }
}
