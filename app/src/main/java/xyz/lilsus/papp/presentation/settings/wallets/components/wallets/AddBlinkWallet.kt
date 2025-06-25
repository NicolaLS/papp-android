package xyz.lilsus.papp.presentation.settings.wallets.components.wallets

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.wallets.BlinkConfig
import xyz.lilsus.papp.presentation.settings.wallets.components.WalletAliasInput

@Composable
fun AddBlinkWallet(onDismiss: () -> Unit, onConfirm: (walletEntry: AddWalletEntry) -> Unit) {
    var alias by remember { mutableStateOf("Blink") }
    var apiKey by remember { mutableStateOf("") }
    var walletId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Blink Wallet") },
        text = {
            Column {
                WalletAliasInput(alias = alias, onAliasChange = { alias = it })
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") }
                )
                OutlinedTextField(
                    value = walletId,
                    onValueChange = { walletId = it },
                    label = { Text("Wallet ID") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    AddWalletEntry(
                        alias, WalletConfigEntry.Blink(
                            BlinkConfig(apiKey, walletId)
                        )
                    )
                )
            }) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}