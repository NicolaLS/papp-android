package xyz.lilsus.papp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import xyz.lilsus.papp.ui.main.MainScreen
import xyz.lilsus.papp.ui.main.MainViewModel

enum class Screen {
    MAIN, SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                App(Screen.MAIN)
            }
        }
    }
}

@Composable
fun App(currentScreen: Screen) {
    when (currentScreen) {
        Screen.MAIN -> {
            val viewModel = remember { MainViewModel() }
            MainScreen(viewModel)
        }
        Screen.SETTINGS -> {
            // SettingsScreen() – will add this later
        }
    }
}
