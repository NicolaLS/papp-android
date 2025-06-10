package xyz.lilsus.papp.data.api.alby


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.lilsus.papp.data.api.base.ApiResponse
import xyz.lilsus.papp.data.api.base.WalletClient
import xyz.lilsus.papp.util.Invoice

// https://guides.getalby.com/developer-guide/alby-wallet-api
class AlbyApiClient : WalletClient {
    override suspend fun payBolt11(invoice: Invoice): ApiResponse =
        withContext(Dispatchers.IO) {
            TODO("Not yet implemented")
        }
}
