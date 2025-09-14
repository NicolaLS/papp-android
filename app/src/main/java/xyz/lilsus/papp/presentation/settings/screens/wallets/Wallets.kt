package xyz.lilsus.papp.presentation.settings.screens.wallets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.settings.components.Bar
import xyz.lilsus.papp.presentation.settings.wallets.WalletSettings

@Composable
fun Wallets(modifier: Modifier = Modifier, viewModel: WalletsViewModel, onBack: () -> Unit) {
    Scaffold(topBar = {
        Bar(
            stringResource(R.string.setting_manage_wallets),
            onBack
        )
    }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            WalletSettings(
                viewModel = viewModel
            )
        }

    }
}