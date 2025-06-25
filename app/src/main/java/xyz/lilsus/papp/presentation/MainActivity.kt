package xyz.lilsus.papp.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import xyz.lilsus.papp.data.WalletConfigStoreSerializer
import xyz.lilsus.papp.data.remote.WalletClientFactoryImpl
import xyz.lilsus.papp.data.repository.WalletConfigRepositoryImpl
import xyz.lilsus.papp.data.repository.WalletRepositoryImpl
import xyz.lilsus.papp.domain.use_case.wallets.PayInvoiceUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.AddWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.GetActiveWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.GetAllWalletsUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.RemoveWalletUseCase
import xyz.lilsus.papp.domain.use_case.wallets.config.SetActiveWalletUseCase
import xyz.lilsus.papp.presentation.main.MainScreen
import xyz.lilsus.papp.presentation.main.MainViewModel
import xyz.lilsus.papp.presentation.settings.SettingsScreen
import xyz.lilsus.papp.presentation.settings.SettingsViewModel
import xyz.lilsus.papp.proto.wallet_config.WalletConfigStore

enum class Screen {
    MAIN, SETTINGS
}

val Context.walletConfigStore: DataStore<WalletConfigStore> by dataStore(
    fileName = "wallet_config.pb",
    serializer = WalletConfigStoreSerializer
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    val context = LocalContext.current

    // Create WalletConfigRepository
    val walletConfigRepository = remember {
        WalletConfigRepositoryImpl(context.walletConfigStore)
    }

    // Create client factory
    val walletClientFactory = remember {
        WalletClientFactoryImpl()
    }
    // Create WalletRepository
    val walletRepository = remember(walletConfigRepository, walletClientFactory) {
        WalletRepositoryImpl(walletConfigRepository, walletClientFactory)
    }

    when (currentScreen) {
        Screen.MAIN -> {
            val payUseCase = remember(walletRepository) {
                PayInvoiceUseCase(walletRepository)
            }
            val viewModel = remember(payUseCase) {
                MainViewModel(payUseCase)
            }
            MainScreen(
                viewModel,
                onSettingsClick = { currentScreen = Screen.SETTINGS }
            )
        }

        Screen.SETTINGS -> {
            val getAllWallets = remember(walletConfigRepository) {
                GetAllWalletsUseCase(walletConfigRepository)
            }
            val getActiveWallet = remember(walletConfigRepository) {
                GetActiveWalletUseCase(walletConfigRepository)
            }
            val setActiveWallet = remember(walletConfigRepository) {
                SetActiveWalletUseCase(walletConfigRepository)
            }
            val removeWallet = remember(walletConfigRepository) {
                RemoveWalletUseCase(walletConfigRepository)
            }

            val addWallet = remember(walletConfigRepository) {
                AddWalletUseCase(walletConfigRepository)
            }

            val viewModel =
                remember(getAllWallets, getActiveWallet, setActiveWallet, removeWallet) {
                    SettingsViewModel(
                        getAllWallets,
                        getActiveWallet,
                        setActiveWallet,
                        removeWallet,
                        addWallet
                    )
                }

            SettingsScreen(
                onBack = { currentScreen = Screen.MAIN },
                viewModel = viewModel
            )
        }
    }
}

