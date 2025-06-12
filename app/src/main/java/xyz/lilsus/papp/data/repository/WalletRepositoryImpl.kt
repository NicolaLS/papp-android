package xyz.lilsus.papp.data.repository

import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.data.remote.blink.BlinkWalletApi
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.repository.WalletRepository

class WalletRepositoryImpl : WalletRepository {
    override suspend fun payBolt11Invoice(invoice: Invoice): SendPaymentResult {
        val client = BlinkWalletApi()
        val res = client.payBolt11Invoice(invoice)
        val data = res.interpretWalletDto()
        return data
    }
}