package xyz.lilsus.papp.data

import xyz.lilsus.papp.util.Invoice

class ApiRepository {
    private val blink = BlinkClient()

    suspend fun payInvoice(paymentRequest: Invoice): PaymentSendPayload {
        return blink.payInvoice(paymentRequest)
    }
}