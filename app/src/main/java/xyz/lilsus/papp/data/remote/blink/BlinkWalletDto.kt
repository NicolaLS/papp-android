package xyz.lilsus.papp.data.remote.blink

import kotlinx.serialization.Serializable
import xyz.lilsus.papp.common.Wallet
import xyz.lilsus.papp.domain.model.IntoSendPaymentResult
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.SendPaymentResult
import kotlin.math.abs

@Serializable
enum class PaymentSendResult { ALREADY_PAID, FAILURE, PENDING, SUCCESS, }

@Serializable
enum class TxDirection { RECEIVE, SEND }

@Serializable
enum class TxStatus { FAILURE, PENDING, SUCCESS }

@Serializable
data class Transaction(
    val direction: TxDirection,
    val memo: String? = null,
    val settlementAmount: Int,
    val settlementFee: Int,
    val status: TxStatus
)

@Serializable
data class Error(
    val code: String? = null,
    val message: String,
    val path: List<String>? = emptyList()
)

@Serializable
data class PaymentSendPayload(
    val errors: List<Error>? = emptyList(),
    val status: PaymentSendResult? = null,
    val transaction: Transaction? = null,
)

@Serializable
data class PayInvoiceData(
    val lnInvoicePaymentSend: PaymentSendPayload
)

@Serializable
data class PayInvoiceResponse(
    val data: PayInvoiceData
) : IntoSendPaymentResult {
    override fun interpretWalletDto(): SendPaymentResult {
        val wallet = Wallet.BLINK
        val payload = data.lnInvoicePaymentSend
        val status =
            payload.status ?: return SendPaymentResult.Failure(wallet, "Missing payment status")

        return when (status) {
            PaymentSendResult.ALREADY_PAID -> SendPaymentResult.AlreadyPaid(wallet)
            PaymentSendResult.FAILURE -> {
                val error = "Error Sending Payment:\n ${payload.errors?.firstOrNull()?.message}"
                SendPaymentResult.Failure(wallet, error)
            }

            PaymentSendResult.PENDING -> SendPaymentResult.Pending(wallet)
            PaymentSendResult.SUCCESS -> {
                val tx = payload.transaction ?: return SendPaymentResult.Failure(
                    wallet,
                    "Missing transaction"
                )
                val amountPaidTotal = abs(tx.settlementAmount)
                val feePaid = abs(tx.settlementFee)
                val amountPaid = amountPaidTotal - feePaid
                val memo = tx.memo

                SendPaymentResult.Success(
                    wallet = wallet,
                    data = SendPaymentData(
                        amountPaid = amountPaid,
                        feePaid = feePaid,
                        memo = memo
                    )
                )
            }
        }
    }
}
