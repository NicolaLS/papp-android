package xyz.lilsus.papp.presentation.settings.wallets.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

@Composable
fun AddWalletSelection(onClick: (type: WalletTypeEntry) -> Unit) {
    Column {
        Button(onClick = { onClick(WalletTypeEntry.BLINK) }) {
            Text("Blink Wallet")
        }
    }
}