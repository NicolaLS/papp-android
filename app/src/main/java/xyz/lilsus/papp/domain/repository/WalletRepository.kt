package xyz.lilsus.papp.domain.repository

import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.amount.SatoshiAmount
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

interface WalletRepository {
    val walletType: WalletTypeEntry
    suspend fun payBolt11Invoice(bolt11Invoice: Invoice.Bolt11): Resource<SendPaymentData, WalletRepositoryError>
    suspend fun probeBolt11PaymentFee(bolt11Invoice: Invoice.Bolt11): Resource<SatoshiAmount, WalletRepositoryError>
}
