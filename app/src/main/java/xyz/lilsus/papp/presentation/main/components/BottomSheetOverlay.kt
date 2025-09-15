package xyz.lilsus.papp.presentation.main.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.lilsus.papp.presentation.main.Intent
import xyz.lilsus.papp.presentation.main.UiState

@Composable
fun BottomSheetOverlay(
    modifier: Modifier = Modifier,
    uiState: UiState,
    onAction: (Intent) -> Unit
) {
    when (uiState) {
        UiState.Active,
        UiState.PerformingPayment,
        is UiState.PaymentResultSuccess,
        is UiState.PaymentResultError -> {
            // No bottom sheet in these states
        }

        is UiState.ConfirmPayment -> {
            ConfirmationBottomSheet(
                uiState = uiState,
                onPay = { onAction(Intent.PayInvoice(uiState.data.invoice)) },
                onDismiss = { onAction(Intent.Dismiss) }
            )
        }

        is UiState.QrDetected -> {
            QrDetectedBottomSheet(
                uiState.invalidInvoice,
                onDismiss = { onAction(Intent.Dismiss) }
            )
        }
    }
}