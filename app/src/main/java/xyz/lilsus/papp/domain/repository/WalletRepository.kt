package xyz.lilsus.papp.domain.repository

import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

interface WalletRepository {
    val walletType: WalletTypeEntry
    suspend fun payBolt11Invoice(bolt11Invoice: Invoice): Result<SendPaymentData>
}