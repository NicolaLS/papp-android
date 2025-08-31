package xyz.lilsus.papp.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.lilsus.papp.data.WalletConfigStoreSerializer.walletConfigStore
import xyz.lilsus.papp.data.repository.SettingsRepositoryImpl
import xyz.lilsus.papp.data.repository.WalletConfigRepositoryImpl
import xyz.lilsus.papp.data.repository.WalletRepositoryFactoryImpl
import xyz.lilsus.papp.data.settingsDataStore
import xyz.lilsus.papp.domain.repository.WalletRepository
import java.util.concurrent.Executors

class AppDependencies(context: Context, private val applicationScope: CoroutineScope) {
    val walletConfigRepository = WalletConfigRepositoryImpl(context.walletConfigStore)
    val settingsRepository = SettingsRepositoryImpl(context.settingsDataStore)

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

    // FIXME
    val analyzerExecutor = Executors.newCachedThreadPool()
    val barcodeScannerExecutor = Executors.newSingleThreadExecutor()
}
