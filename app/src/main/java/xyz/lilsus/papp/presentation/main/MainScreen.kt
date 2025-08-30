package xyz.lilsus.papp.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.lilsus.papp.presentation.main.components.QrCodeBottomSheet

@Composable
fun MainScreen(viewModel: MainViewModel, onSettingsClick: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val uiState by viewModel.uiState

    val showBottomSheet = when (uiState) {
        is PaymentUiState.Loading,
        is PaymentUiState.Received,
        is PaymentUiState.Error -> true

        PaymentUiState.Idle -> false
    }


    LaunchedEffect(Unit) { viewModel.bindCamera(context, lifecycleOwner) }

    if (showBottomSheet) {
        QrCodeBottomSheet(
            uiState = uiState,
            onDismiss = viewModel::reset
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        ExtendedFloatingActionButton(
            onClick = onSettingsClick,
            icon = { Icon(Icons.Filled.Settings, "Extended floating action button.") },
            text = { Text(text = "Settings") },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
        )
    }
}