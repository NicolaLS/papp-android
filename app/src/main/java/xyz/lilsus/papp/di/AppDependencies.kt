package xyz.lilsus.papp.di

import android.content.Context
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.lilsus.papp.common.InvoiceAnalyzer
import xyz.lilsus.papp.common.InvoiceParser
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


    // Barcode Scanner and Image Analyzer
    private val qrCodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    private val invoiceParser = InvoiceParser()
    private val analyzer = InvoiceAnalyzer(qrCodeScanner, invoiceParser)

    private val imageAnalysisUseCase = ImageAnalysis.Builder()
        .setResolutionSelector(
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    // NOTE: MlKit recommends around 1920x1080 resolution which is 16:9.
                    // But we do not need a WYSIWYG experience so we prefer the most common
                    // native sensor aspect ratio which is 4:3.
                    ResolutionStrategy(
                        Size(1920, 1440),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER,
                    )
                )
                .build()
        )
        .build()

    // FIXME: Right now the view model never gets cleaned up on 'navigation'
    // to the settings. If we'd define the dependencies outside of this function
    fun createMainViewModel(walletRepository: WalletRepository?): MainViewModel {
        val payUseCase = PayInvoiceUseCase(walletRepository)
        return MainViewModel(payUseCase, imageAnalysisUseCase, analyzer)
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
