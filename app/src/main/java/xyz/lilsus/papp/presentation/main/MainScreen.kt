package xyz.lilsus.papp.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.lilsus.papp.presentation.main.components.ConfirmationBottomSheet
import xyz.lilsus.papp.presentation.main.components.PaymentError
import xyz.lilsus.papp.presentation.main.components.PaymentResultScreen
import xyz.lilsus.papp.presentation.main.components.QrDetectedBottomSheet
import xyz.lilsus.papp.presentation.main.components.hero.Hero


@Composable
fun MainScreen(viewModel: MainViewModel, onSettingsClick: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val uiState = viewModel.uiState

    LaunchedEffect(Unit) { viewModel.dispatch(Intent.BindCamera(context, lifecycleOwner)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero Guide (no user interaction)
            val heroColor = when (uiState) {
                UiState.Active -> MaterialTheme.colorScheme.onSurfaceVariant
                is UiState.QrDetected,
                is UiState.ConfirmPayment -> MaterialTheme.colorScheme.primary

                is UiState.PaymentDone -> when (uiState.result) {
                    is PaymentResult.Success -> Color(0xFF4CAF50)
                    is PaymentResult.Error -> MaterialTheme.colorScheme.error
                }

                UiState.PerformingPayment -> MaterialTheme.colorScheme.tertiary
            }
            Hero(
                modifier = Modifier
                    .size(192.dp)
                    .weight(1f),
                color = heroColor,
                uiState = uiState,
            )

            // User Interaction Section
            Column(modifier = Modifier.weight(2f)) {
                when (uiState) {
                    is UiState.ConfirmPayment -> {
                        ConfirmationBottomSheet(
                            uiState = uiState,
                            onPay = { viewModel.dispatch(Intent.PayInvoice(uiState.data.invoice)) },
                            onDismiss = { viewModel.dispatch(Intent.Dismiss) }
                        )
                    }

                    is UiState.PaymentDone -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (val result = uiState.result) {
                                is PaymentResult.Error -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        PaymentError(result.message)
                                    }
                                }

                                is PaymentResult.Success -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        PaymentResultScreen(result.data.first)
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Button(onClick = { viewModel.dispatch(Intent.Dismiss) }) {
                                Text("Dismiss")
                            }
                        }
                    }

                    is UiState.QrDetected -> {
                        QrDetectedBottomSheet(
                            uiState.invalidInvoice,
                            onDismiss = { viewModel.dispatch(Intent.Dismiss) }
                        )
                    }
                    /* no user interaction */
                    UiState.Active -> {}
                    UiState.PerformingPayment -> {}
                }
            }

        }
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