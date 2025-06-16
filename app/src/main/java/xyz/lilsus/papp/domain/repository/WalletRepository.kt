package xyz.lilsus.papp.domain.repository

import kotlinx.coroutines.flow.Flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.proto.wallet_config.WalletConfig

interface WalletRepository {
    suspend fun payBolt11Invoice(invoice: Invoice): SendPaymentResult
    suspend fun updateWalletConfig(newConfig: WalletConfig)
    
    val walletConfigFlow: Flow<WalletConfig>
}