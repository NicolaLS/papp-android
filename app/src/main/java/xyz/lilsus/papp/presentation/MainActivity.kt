package xyz.lilsus.papp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import xyz.lilsus.papp.presentation.main.MainScreen
import xyz.lilsus.papp.presentation.main.MainViewModel
import xyz.lilsus.papp.presentation.main.components.WithCameraPermission
import xyz.lilsus.papp.presentation.settings.SettingsScreen
import xyz.lilsus.papp.presentation.settings.SettingsViewModel
import xyz.lilsus.papp.presentation.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
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
                ) { navController.navigate(RSettings) }
            }
        }
        composable<RSettings> { backStackEntry ->
            SettingsScreen(
                onBack = { navController.navigate(RMain) },
                viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = SettingsViewModel.Factory,
                )
            )
        }
    }
}

