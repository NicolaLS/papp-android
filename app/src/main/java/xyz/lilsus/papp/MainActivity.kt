package xyz.lilsus.papp

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
import xyz.lilsus.papp.ui.main.MainScreen
import xyz.lilsus.papp.ui.main.MainViewModel
import xyz.lilsus.papp.ui.settings.SettingsScreen

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
            val viewModel = remember { MainViewModel() }
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

