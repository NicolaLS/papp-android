package xyz.lilsus.papp.util

import app.cash.lninvoice.InvalidInvoice
import app.cash.lninvoice.PaymentRequest
import arrow.core.Either

// Store encoded bolt11 invoice and decoded payment request. By using a private constructor we can
// guarantee that the values are correct. So Bolt11Invoice("invalid", validPaymentRequest)
// can not happen.
sealed class Bolt11Invoice private constructor(
    val encodedSafe: String,
    val paymentRequest: PaymentRequest
) {
    companion object {
        fun parseOrNull(encodedUnsafe: String): Bolt11Invoice? {
            val request: Either<InvalidInvoice, PaymentRequest> =
                PaymentRequest.parse(encodedUnsafe)

            return when (request) {
                is Either.Right -> SafeBolt11Invoice(encodedUnsafe, request.value)
                is Either.Left -> null
            }
        }

        private class SafeBolt11Invoice(encodedSafe: String, paymentRequest: PaymentRequest) :
            Bolt11Invoice(encodedSafe, paymentRequest)
    }
}