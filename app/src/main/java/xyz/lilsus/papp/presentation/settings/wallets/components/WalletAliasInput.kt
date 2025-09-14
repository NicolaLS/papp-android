package xyz.lilsus.papp.presentation.settings.wallets.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R

@Composable
fun WalletAliasInput(alias: String, onAliasChange: (String) -> Unit) {
    OutlinedTextField(
        value = alias,
        onValueChange = onAliasChange,
        label = { Text(stringResource(R.string.alias_label)) },
        modifier = Modifier.fillMaxWidth()
    )
}