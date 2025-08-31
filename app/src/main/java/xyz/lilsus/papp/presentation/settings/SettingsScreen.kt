package xyz.lilsus.papp.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.presentation.SettingsDestination
import xyz.lilsus.papp.presentation.settings.components.Bar
import xyz.lilsus.papp.presentation.settings.components.Setting

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (SettingsDestination) -> Unit,
    viewModel: SettingsViewModel
) {
    val walletSubtitle by viewModel.activeWalletSubtitle.collectAsState()

    Scaffold(
        topBar = { Bar("Settings", onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Setting(title = "Manage Wallets", subtitle = walletSubtitle, onClick = {
                onNavigate(SettingsDestination.Wallets)
            })
            Setting(title = "Payments", onClick = { onNavigate(SettingsDestination.Payments) })
            Setting(
                title = "Currency",
                subtitle = "BTC",
                onClick = { onNavigate(SettingsDestination.Currency) })
            Setting(title = "Language", subtitle = "English", onClick = {
                onNavigate(
                    SettingsDestination.Language
                )
            })
        }
    }
}