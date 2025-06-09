package xyz.lilsus.papp.data

class ApiRepository {
    private val blink = BlinkClient()

    suspend fun payInvoice(paymentRequest: String): PaymentSendPayload {
        return blink.payInvoice(paymentRequest)
    }
}