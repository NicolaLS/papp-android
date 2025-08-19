package xyz.lilsus.papp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import xyz.lilsus.papp.di.AppDependencies
import xyz.lilsus.papp.di.PappApplication
import xyz.lilsus.papp.presentation.main.MainScreen
import xyz.lilsus.papp.presentation.main.components.WithCameraPermission
import xyz.lilsus.papp.presentation.settings.SettingsScreen

enum class Screen {
    MAIN, SETTINGS
}


class MainActivity : ComponentActivity() {
    private lateinit var appDependencies: AppDependencies

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Access the app-level container
        appDependencies = (application as PappApplication).appDependencies

        setContent {
            MaterialTheme {
                App(appDependencies)
            }
        }
    }
}

@Composable
fun App(deps: AppDependencies) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }

    // Observe latest wallet repository
    val walletRepository by deps.walletRepositoryFlow.collectAsState()

    when (currentScreen) {
        Screen.MAIN -> {

            val viewModel = remember(walletRepository) {
                deps.createMainViewModel(walletRepository)
            }
            WithCameraPermission {
                MainScreen(viewModel) {
                    currentScreen = Screen.SETTINGS
                }
            }

        }

        Screen.SETTINGS -> {
            val viewModel = remember {
                deps.createSettingsViewModel()
            }
            SettingsScreen(
                viewModel = viewModel,
                onBack = { currentScreen = Screen.MAIN }
            )
        }
    }
}

