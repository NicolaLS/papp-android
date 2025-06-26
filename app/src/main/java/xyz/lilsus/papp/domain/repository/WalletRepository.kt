package xyz.lilsus.papp.domain.repository

import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.SendPaymentResult

interface WalletRepository {
    suspend fun payBolt11Invoice(bolt11Invoice: Invoice): SendPaymentResult
}