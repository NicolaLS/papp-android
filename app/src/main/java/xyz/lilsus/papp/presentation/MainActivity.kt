package xyz.lilsus.papp.presentation

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
import xyz.lilsus.papp.data.repository.WalletRepositoryImpl
import xyz.lilsus.papp.domain.use_case.pay.PayInvoiceUseCase
import xyz.lilsus.papp.presentation.main.MainScreen
import xyz.lilsus.papp.presentation.main.MainViewModel
import xyz.lilsus.papp.presentation.settings.SettingsScreen

enum class Screen {
    MAIN, SETTINGS
}

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

    when (currentScreen) {
        Screen.MAIN -> {
            val walletRepository = WalletRepositoryImpl()
            val payUseCase = PayInvoiceUseCase(walletRepository)
            val viewModel = remember { MainViewModel(payUseCase) }
            MainScreen(
                viewModel,
                onSettingsClick = { currentScreen = Screen.SETTINGS }
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(onBack = { currentScreen = Screen.MAIN })
        }
    }
}

