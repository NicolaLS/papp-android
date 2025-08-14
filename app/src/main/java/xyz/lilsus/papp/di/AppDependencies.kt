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
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.AddWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.GetActiveWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.GetAllWalletsUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.RemoveWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.SetActiveWalletUseCase
import xyz.lilsus.papp.presentation.main.MainViewModel
import xyz.lilsus.papp.presentation.settings.SettingsViewModel
import xyz.lilsus.papp.proto.wallet_config.WalletConfigStore
import java.util.concurrent.Executors

class AppDependencies(val context: Context, private val applicationScope: CoroutineScope) {
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

    fun createMainViewModel(walletRepository: WalletRepository?): MainViewModel {
        val payUseCase = PayInvoiceUseCase(walletRepository)
        val cameraController = CameraControllerFactory(context).createController()
        val analyzer = QrCodeAnalyzer()
        cameraController.setImageAnalysisAnalyzer(
            Executors.newCachedThreadPool(),
            analyzer
        )
        return MainViewModel(payUseCase, analyzer, cameraController)
    }

    fun createSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            GetAllWalletsUseCase(walletConfigRepository),
            GetActiveWalletUseCase(walletConfigRepository),
            SetActiveWalletUseCase(walletConfigRepository),
            RemoveWalletUseCase(walletConfigRepository),
            AddWalletUseCase(walletConfigRepository)
        )
    }
}
