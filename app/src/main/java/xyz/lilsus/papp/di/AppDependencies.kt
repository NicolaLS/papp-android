package xyz.lilsus.papp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.lilsus.papp.data.WalletConfigStoreSerializer
import xyz.lilsus.papp.data.repository.WalletConfigRepositoryImpl
import xyz.lilsus.papp.data.repository.WalletRepositoryFactoryImpl
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.proto.wallet_config.WalletConfigStore
import java.util.concurrent.Executors

class AppDependencies(context: Context, private val applicationScope: CoroutineScope) {
    private val Context.walletConfigStore: DataStore<WalletConfigStore> by dataStore(
        fileName = "wallet_config.pb",
        serializer = WalletConfigStoreSerializer
    )

    private val dataStore: DataStore<WalletConfigStore> = context.walletConfigStore

    val walletConfigRepository = WalletConfigRepositoryImpl(dataStore)
    private val walletRepositoryFactory = WalletRepositoryFactoryImpl()

    // Expose a StateFlow of the WalletRepository (auto-updating client)
    val walletRepositoryFlow: StateFlow<WalletRepository?> =
        walletConfigRepository.activeWalletConfigOrNull
            .map { it?.let { walletRepositoryFactory.getClientFromConfigOrNull(it) } }
            .stateIn(
                scope = applicationScope,
                started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
                initialValue = null
            )

    val analyzerExecutor = Executors.newCachedThreadPool()
    val barcodeScannerExecutor = Executors.newSingleThreadExecutor()
}
