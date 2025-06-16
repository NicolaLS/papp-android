package xyz.lilsus.papp.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val walletId by viewModel.walletId.collectAsState()
    val status by viewModel.statusMessage.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Connect Blink Wallet")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Status: $status")
        Spacer(modifier = Modifier.height(8.dp))

        if (isConnected) {
            // Show only disconnect button when connected
            Button(
                onClick = { viewModel.disconnectWallet() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Disconnect")
            }
        } else {
            // Show inputs + connect button when not connected
            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.onApiKeyChange(it) },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = walletId,
                onValueChange = { viewModel.onWalletIdChange(it) },
                label = { Text("Wallet-ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.connectBlinkWallet() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Connect")
            }
        }
    }
}
