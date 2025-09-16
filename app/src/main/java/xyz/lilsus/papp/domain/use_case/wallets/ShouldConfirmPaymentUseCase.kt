package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.amount.SatoshiAmount
import xyz.lilsus.papp.domain.repository.SettingsRepository
import xyz.lilsus.papp.presentation.model.PaymentError

data class InvoiceConfirmationData(
    val invoice: Invoice.Bolt11,
    val amount: SatoshiAmount,
    val feeFlow: Flow<Resource<SatoshiAmount, PaymentError>>
)

sealed class ShouldConfirmPaymentResult {
    data class ConfirmationRequired(val data: InvoiceConfirmationData) :
        ShouldConfirmPaymentResult()

    object ConfirmationNotRequired : ShouldConfirmPaymentResult()
}

class ShouldConfirmPaymentUseCase(
    private val settingsRepository: SettingsRepository,
    private val probeFee: ProbeFeeUseCase,
) {
    suspend operator fun invoke(
        invoice: Invoice.Bolt11,
    ): ShouldConfirmPaymentResult {
        val paymentSettings = settingsRepository.paymentSettings.firstOrNull()
        return if (
            paymentSettings == null ||
            paymentSettings.alwaysConfirmPayment ||
            // Only if invoice amount is *above* the configured amount.
            paymentSettings.confirmPaymentAbove < invoice.amountSatoshi
        ) {
            ShouldConfirmPaymentResult.ConfirmationRequired(
                InvoiceConfirmationData(
                    invoice,
                    SatoshiAmount(invoice.amountSatoshi),
                    flow { emit(probeFee(invoice)) }
                )
            )
        } else {
            ShouldConfirmPaymentResult.ConfirmationNotRequired
        }
    }
}