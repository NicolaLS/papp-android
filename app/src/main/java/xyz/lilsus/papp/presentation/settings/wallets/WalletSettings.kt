package xyz.lilsus.papp.presentation.settings.wallets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.presentation.settings.screens.wallets.WalletOption
import xyz.lilsus.papp.presentation.settings.screens.wallets.WalletsViewModel
import xyz.lilsus.papp.presentation.settings.wallets.components.AddWalletModal
import xyz.lilsus.papp.presentation.settings.wallets.components.WalletSelectionDropdown

@Composable
fun WalletSettings(
    modifier: Modifier = Modifier,
    viewModel: WalletsViewModel
) {
    val allWallets by viewModel.allWallets.collectAsState()
    val selected by viewModel.selectedWallet.collectAsState()


    Text("Wallet Settings", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(16.dp))

    when (val result = allWallets) {
        is Resource.Loading -> CircularProgressIndicator()
        is Resource.Error -> Text("Error loading wallets")
        is Resource.Success -> {
            val wallets = result.data

            val walletOptions = buildList {
                add(WalletOption.None)
                addAll(wallets.map { WalletOption.Wallet(it) })
            }


            WalletSelectionDropdown(walletOptions, selected, viewModel::onWalletSelected)

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = viewModel::onRemoveSelected,
                    enabled = selected is WalletOption.Wallet
                ) {
                    Text("Remove")
                }

                Button(onClick = viewModel::onAddWalletClicked) {
                    Text("Add Wallet")
                }
            }
        }
    }
    if (viewModel.showWalletTypeModal) {
        AddWalletModal(
            onDismiss = { viewModel.showWalletTypeModal = false },
            onConfirm = { wallet ->
                viewModel.connectWallet(wallet)
            })
    }
}