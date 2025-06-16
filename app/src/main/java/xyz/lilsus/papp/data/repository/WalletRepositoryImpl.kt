package xyz.lilsus.papp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.data.WalletConfigSerializer
import xyz.lilsus.papp.data.remote.blink.BlinkWalletApi
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.repository.WalletApi
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.proto.wallet_config.WalletConfig
import xyz.lilsus.papp.proto.wallet_config.WalletType
import java.io.File
import java.io.IOException

class WalletRepositoryImpl(
    context: Context
) : WalletRepository {

    private val walletConfigStore: DataStore<WalletConfig> = DataStoreFactory.create(
        serializer = WalletConfigSerializer,
        produceFile = { File(context.filesDir, "wallet_config.pb") }
    )

    private var walletClient: WalletApi? = null
    private var walletConfig: WalletConfig? = null

    private suspend fun ensureClientInitialized() {
        if (walletClient == null) {
            val config = walletConfigStore.data.first()
            if (config.activeWalletType == WalletType.WALLET_TYPE_NONE) {
                throw IllegalStateException("No active wallet configured")
            }
            walletConfig = config

        }
    }

    override suspend fun updateWalletConfig(newConfig: WalletConfig) {
        walletConfigStore.updateData { newConfig }
        walletConfig = newConfig
        // TODO: Re-initialize wallet client using factory and config.
    }

    override suspend fun payBolt11Invoice(invoice: Invoice): SendPaymentResult {
        // TODO: Use factory with config wallet type.
        ensureClientInitialized()
        val client = BlinkWalletApi(walletConfig!!.blinkWallet)
        val res = client.payBolt11Invoice(invoice)
        val data = res.interpretWalletDto()
        return data
    }

    override val walletConfigFlow: Flow<WalletConfig> = walletConfigStore.data
        .catch { e ->
            if (e is IOException) emit(WalletConfig.getDefaultInstance())
            else throw e
        }
        .distinctUntilChanged()
}