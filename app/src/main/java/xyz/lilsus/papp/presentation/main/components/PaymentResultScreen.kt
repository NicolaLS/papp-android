package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.main.UiState
import xyz.lilsus.papp.presentation.model.PaymentData

@Composable
fun PaymentResultScreen(state: UiState.PaymentResultSuccess) {
    val result: PaymentData = state.data
    val displayCurrency by state.displayCurrency.collectAsState()
    val display by state.display.collectAsState()

    val title = when (result) {
        is PaymentData.AlreadyPaid -> stringResource(R.string.invoice_already_paid_title)
        is PaymentData.Pending -> stringResource(R.string.payment_pending_title)
        is PaymentData.Paid -> {
            val amountFmtOrNull = remember(displayCurrency, display, result) {
                val er = display.exchangeRate
                if (displayCurrency is xyz.lilsus.papp.domain.model.amount.DisplayCurrency.Fiat && er == null) {
                    null
                } else {
                    runCatching { displayCurrency.format(result.amountPaid, er) }.getOrNull()
                }
            }
            amountFmtOrNull ?: stringResource(R.string.loading)
        }
    }

    val subtitle = when (result) {
        is PaymentData.Paid -> {
            val feeFmtOrNull = remember(displayCurrency, display, result) {
                val er = display.exchangeRate
                if (displayCurrency is xyz.lilsus.papp.domain.model.amount.DisplayCurrency.Fiat && er == null) {
                    null
                } else {
                    runCatching { displayCurrency.format(result.feePaid, er) }.getOrNull()
                }
            }
            val feeText = feeFmtOrNull ?: stringResource(R.string.loading)
            "${stringResource(R.string.fee_label)} $feeText"
        }
        else -> null
    }

    BottomLayout(
        modifier = Modifier.fillMaxWidth(),
        title = title,
        subtitle = subtitle,
    )
}