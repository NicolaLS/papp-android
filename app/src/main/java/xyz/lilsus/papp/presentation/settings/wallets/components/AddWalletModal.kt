package xyz.lilsus.papp.presentation.settings.wallets.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.presentation.settings.wallets.components.wallets.AddBlinkWallet

@Composable
fun AddWalletModal(onDismiss: () -> Unit, onConfirm: (AddWalletEntry) -> Unit) {
    var selectedWalletType by remember { mutableStateOf<WalletTypeEntry>(WalletTypeEntry.NOT_SET) }
    AlertDialog(
        onDismissRequest = { onDismiss },
        title = { Text("Select Wallet Type") },
        text = {
            when (selectedWalletType) {
                WalletTypeEntry.NOT_SET -> AddWalletSelection(onClick = { walletType ->
                    selectedWalletType = walletType
                })

                WalletTypeEntry.BLINK -> AddBlinkWallet(onDismiss, onConfirm)
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}