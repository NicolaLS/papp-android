package xyz.lilsus.papp.util

import app.cash.lninvoice.InvalidInvoice
import app.cash.lninvoice.PaymentRequest
import arrow.core.Either

// BOLT11 Invoice as encoded String and decoded PaymentRequest.
// Sealed class with private constructor guarantees correctness.
sealed class Invoice private constructor(
    val encodedSafe: String,
    val paymentRequest: PaymentRequest
) {
    companion object {
        fun parseOrNull(encodedUnsafe: String): Invoice? {
            val request: Either<InvalidInvoice, PaymentRequest> =
                PaymentRequest.parse(encodedUnsafe)

            return when (request) {
                is Either.Right -> SafeBolt11Invoice(encodedUnsafe, request.value)
                is Either.Left -> null
            }
        }

        private class SafeBolt11Invoice(encodedSafe: String, paymentRequest: PaymentRequest) :
            Invoice(encodedSafe, paymentRequest)
    }
}