package xyz.lilsus.papp.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import xyz.lilsus.papp.presentation.main.components.AnimatedAbstractQr
import xyz.lilsus.papp.presentation.main.components.QrCodeBottomSheet
import xyz.lilsus.papp.presentation.main.components.QrState

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

    val state = remember { mutableStateOf(QrState.ACTIVE) }
    LaunchedEffect(Unit) {
        delay(10000)
        state.value = QrState.DETECTED

    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedAbstractQr(modifier = Modifier.size(254.dp), state = state.value)
    }


    if (showBottomSheet) {
        QrCodeBottomSheet(
            uiState = uiState,
            onDismiss = viewModel::reset
        )
    }
}