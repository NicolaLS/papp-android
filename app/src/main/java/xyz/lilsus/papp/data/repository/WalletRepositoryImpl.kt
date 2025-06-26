package xyz.lilsus.papp.data.repository

import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.repository.client.WalletApi

class WalletRepositoryImpl(
    private val client: WalletApi?
) : WalletRepository {

    override suspend fun payBolt11Invoice(invoice: Invoice): SendPaymentResult {
        val res = client?.payBolt11Invoice(invoice)
            ?: throw IllegalStateException("No active wallet configured")
        val data = res.interpretWalletDto()
        return data
    }

}