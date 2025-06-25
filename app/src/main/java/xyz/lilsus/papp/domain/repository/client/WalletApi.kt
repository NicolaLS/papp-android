package xyz.lilsus.papp.domain.repository.client

import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.IntoSendPaymentResult

interface WalletApi {
    suspend fun payBolt11Invoice(bolt11Invoice: Invoice): IntoSendPaymentResult
}