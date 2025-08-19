package xyz.lilsus.papp.domain.repository

import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

interface WalletRepository {
    val walletType: WalletTypeEntry
    suspend fun payBolt11Invoice(bolt11Invoice: Bolt11Invoice): Result<SendPaymentData>
}