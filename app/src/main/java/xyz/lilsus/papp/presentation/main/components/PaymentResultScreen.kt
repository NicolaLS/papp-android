package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.domain.model.amount.Formatter
import xyz.lilsus.papp.presentation.main.UiState
import xyz.lilsus.papp.presentation.model.PaymentData

@Composable
fun PaymentResultScreen(state: UiState.PaymentResultSuccess, formatter: Formatter?) {
    val result: PaymentData = state.data

    val title = when (result) {
        is PaymentData.AlreadyPaid -> stringResource(R.string.invoice_already_paid_title)
        is PaymentData.Pending -> stringResource(R.string.payment_pending_title)
        is PaymentData.Paid -> {
            val amountTextOrNull = remember(formatter, result.amountPaid) {
                formatter?.format(result.amountPaid)
            }
            amountTextOrNull ?: stringResource(R.string.loading)
        }
    }

    val subtitle = when (result) {
        is PaymentData.Paid -> {
            val feeTextOrNull = remember(formatter, result.feePaid) {
                formatter?.format(result.feePaid)
            }
            val feeText = feeTextOrNull ?: stringResource(R.string.loading)
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