package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository

class PayInvoiceUseCase(private val repositoryFlow: StateFlow<WalletRepository?>) {
    operator fun invoke(invoice: Invoice.Bolt11): Flow<Resource<Pair<SendPaymentData, WalletTypeEntry>>> =
        flow {
            emit(Resource.Loading())
            when (repositoryFlow.value) {
                null -> emit(Resource.Error(message = "No active wallet configured"))
                else -> try {
                    repositoryFlow.value?.apply {
                        payBolt11Invoice(invoice.bolt11).fold(
                            onSuccess = {
                                emit(
                                    Resource.Success(
                                        Pair(
                                            it,
                                            this.walletType
                                        )
                                    )
                                )
                            },
                            onFailure = {
                                emit(
                                    Resource.Error(
                                        it.message ?: "Something went wrong"
                                    )
                                )
                            },
                        )

                    }

                } catch (e: Exception) {
                    emit(Resource.Error(message = "Something went wrong...\n${e.message}"))
                }
            }
        }
}