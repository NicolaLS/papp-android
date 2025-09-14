package xyz.lilsus.papp.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import xyz.lilsus.papp.presentation.main.MainScreen
import xyz.lilsus.papp.presentation.main.MainViewModel
import xyz.lilsus.papp.presentation.main.components.WithCameraPermission
import xyz.lilsus.papp.presentation.settings.SettingsScreen
import xyz.lilsus.papp.presentation.settings.SettingsViewModel
import xyz.lilsus.papp.presentation.settings.screens.currency.Currency
import xyz.lilsus.papp.presentation.settings.screens.language.Language
import xyz.lilsus.papp.presentation.settings.screens.language.LanguageViewModel
import xyz.lilsus.papp.presentation.settings.screens.payments.Payments
import xyz.lilsus.papp.presentation.settings.screens.payments.PaymentsViewModel
import xyz.lilsus.papp.presentation.settings.screens.wallets.Wallets
import xyz.lilsus.papp.presentation.settings.screens.wallets.WalletsViewModel
import xyz.lilsus.papp.presentation.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(tonalElevation = 5.dp) {
                    App()
                }
            }
        }
    }
}

@Serializable
object RMain

@Serializable
object RSettings

@Serializable
object NSettings

sealed class SettingsDestination {
    @Serializable
    object Wallets : SettingsDestination()

    @Serializable
    object Payments : SettingsDestination()

    @Serializable
    object Currency : SettingsDestination()

    @Serializable
    object Language : SettingsDestination()
}


@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RMain
    ) {
        composable<RMain> { backStackEntry ->
            WithCameraPermission {
                MainScreen(
                    viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = MainViewModel.Factory,
                    )
                ) { navController.navigate(NSettings) }
            }
        }
        navigation<NSettings>(startDestination = RSettings) {
            // This gets the NavBackStackEntry for the whole graph
            composable<RSettings> { backStackEntry ->
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigate = { dest -> navController.navigate(route = dest) },
                    viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = SettingsViewModel.Factory,
                    )
                )
            }
            composable<SettingsDestination.Wallets> { backStackEntry ->
                Wallets(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = WalletsViewModel.Factory,
                    )
                )
            }
            composable<SettingsDestination.Payments> { backStackEntry ->
                Payments(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = PaymentsViewModel.Factory
                    )
                )
            }
            composable<SettingsDestination.Currency> { backStackEntry ->
                Currency(onBack = { navController.popBackStack() })
            }
            composable<SettingsDestination.Language> { backStackEntry ->
                Language(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = LanguageViewModel.Factory
                    )
                )
            }

        }
    }
}
