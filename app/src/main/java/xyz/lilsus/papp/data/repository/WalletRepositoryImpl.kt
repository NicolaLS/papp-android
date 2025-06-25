package xyz.lilsus.papp.data.repository

import kotlinx.coroutines.launch
import xyz.lilsus.papp.common.AppScope
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.repository.WalletConfigRepository
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.repository.client.WalletApi
import xyz.lilsus.papp.domain.repository.client.WalletClientFactory

class WalletRepositoryImpl(
    walletConfigRepository: WalletConfigRepository,
    walletClientFactory: WalletClientFactory
) : WalletRepository {

    private val configRepository = walletConfigRepository

    private var walletClient: WalletApi? = null

    init {
        AppScope.launch {
            configRepository.getActiveWalletOrNull().collect { wallet ->
                walletClient = walletClientFactory.getClientFromConfigOrNull(wallet)
            }

        }
    }

    override suspend fun payBolt11Invoice(invoice: Invoice): SendPaymentResult {
        val res = walletClient?.payBolt11Invoice(invoice)
            ?: throw IllegalStateException("No active wallet configured")
        val data = res.interpretWalletDto()
        return data
    }

}