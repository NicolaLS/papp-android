package xyz.lilsus.papp.data.repository.blink.dto

import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletError
import kotlin.math.abs

fun PayInvoiceResponse.parse(): Result<SendPaymentData> {
    val payload = this.data.lnInvoicePaymentSend

    val status = payload.status
        ?: return Result.failure(WalletError.MissingStatus)

    return when (status) {
        PaymentSendResult.ALREADY_PAID -> {
            Result.success(SendPaymentData.AlreadyPaid)
        }

        PaymentSendResult.PENDING -> {
            Result.success(SendPaymentData.Pending)
        }

        PaymentSendResult.FAILURE -> {
            val errorMessage = payload.errors?.firstOrNull()?.message
                ?: "Unknown error"
            Result.failure(WalletError.PaymentError("Error sending payment: $errorMessage"))
        }

        PaymentSendResult.SUCCESS -> {
            val tx = payload.transaction
                ?: return Result.failure(WalletError.MissingTransaction)
            val amountPaidTotal = abs(tx.settlementAmount)
            val feePaid = abs(tx.settlementFee)
            val amountPaid = amountPaidTotal - feePaid
            val memo = tx.memo

            Result.success(
                SendPaymentData.Success(
                    amountPaid = amountPaid,
                    feePaid = feePaid,
                    memo = memo
                )
            )
        }
    }
}
