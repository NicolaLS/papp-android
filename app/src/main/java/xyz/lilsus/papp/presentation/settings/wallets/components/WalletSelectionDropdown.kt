package xyz.lilsus.papp.presentation.settings.wallets.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import xyz.lilsus.papp.presentation.settings.screens.wallets.WalletOption

@Composable
fun WalletSelectionDropdown(
    options: List<WalletOption>,
    selected: WalletOption,
    onSelected: (WalletOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(labelForSelection(selected))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    text = { Text(labelForSelection(option)) }
                )
            }
        }
    }
}

fun labelForSelection(option: WalletOption): String = when (option) {
    WalletOption.None -> "None"
    is WalletOption.Wallet -> "${option.entry.alias} (${option.entry.key.take(4)})"
}