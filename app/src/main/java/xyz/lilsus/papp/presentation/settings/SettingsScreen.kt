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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.R
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
    val languageTag = viewModel.activeLanguageTag

    val languageSubtitle = when (languageTag) {
        "en" -> "English"
        "de" -> "Deutsch"
        "es" -> "Español"
        else -> "English"
    }

    Scaffold(
        topBar = { Bar(stringResource(R.string.settings), onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Setting(
                title = stringResource(R.string.setting_manage_wallets),
                subtitle = walletSubtitle,
                onClick = {
                    onNavigate(SettingsDestination.Wallets)
                })
            Setting(
                title = stringResource(R.string.setting_payments),
                onClick = { onNavigate(SettingsDestination.Payments) })
            Setting(
                title = stringResource(R.string.currency),
                subtitle = "BTC",
                onClick = { onNavigate(SettingsDestination.Currency) })
            Setting(
                title = stringResource(R.string.language),
                subtitle = languageSubtitle,
                onClick = {
                    onNavigate(
                        SettingsDestination.Language
                    )
                })
        }
    }
}
