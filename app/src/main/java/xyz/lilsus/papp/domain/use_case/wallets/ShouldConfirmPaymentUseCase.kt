package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.WalletResource
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.domain.use_case.CreateDisplayAmountUseCase
import xyz.lilsus.papp.presentation.model.format
import xyz.lilsus.papp.domain.model.Resource

 data class InvoiceConfirmationData(
    val invoice: Invoice.Bolt11,
    val amountFormatted: String,
    val feeFlow: Flow<WalletResource<String>>
)

sealed class ShouldConfirmPaymentResult {
    data class ConfirmationRequired(val data: InvoiceConfirmationData) :
        ShouldConfirmPaymentResult()

    object ConfirmationNotRequired : ShouldConfirmPaymentResult()
}

class ShouldConfirmPaymentUseCase(
    private val settingsRepository: SettingsRepository,
    private val probeFee: ProbeFeeUseCase,
    private val createDisplayAmount: CreateDisplayAmountUseCase,
) {
    suspend operator fun invoke(
        invoice: Invoice.Bolt11,
    ): ShouldConfirmPaymentResult {
        val paymentSettings = settingsRepository.paymentSettings.firstOrNull()
        return if (
            paymentSettings == null ||
            paymentSettings.alwaysConfirmPayment ||
            paymentSettings.confirmPaymentAbove < invoice.amountSatoshi
        ) {
            val amountFormatted = createDisplayAmount(invoice.amountSatoshi).format()
            ShouldConfirmPaymentResult.ConfirmationRequired(
                InvoiceConfirmationData(
                    invoice,
                    amountFormatted,
                    flow {
                        val feeRes = probeFee(invoice)
                        val mapped: WalletResource<String> = when (feeRes) {
                            is Resource.Success -> {
                                val (feeSat, wallet) = feeRes.data
                                val formatted = createDisplayAmount(feeSat).format()
                                Resource.Success(formatted to wallet)
                            }
                            is Resource.Error -> feeRes
                            is Resource.Loading -> Resource.Loading
                        }
                        emit(mapped)
                    }
                )
            )
        } else {
            ShouldConfirmPaymentResult.ConfirmationNotRequired
        }
    }
}