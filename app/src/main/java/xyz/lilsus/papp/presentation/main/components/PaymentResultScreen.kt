package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.model.PaymentData

@Composable
fun PaymentResultScreen(result: PaymentData) {
    val title = when (result) {
        is PaymentData.AlreadyPaid -> stringResource(R.string.invoice_already_paid_title)
        is PaymentData.Pending -> stringResource(R.string.payment_pending_title)
        is PaymentData.Paid -> result.amountPaid.format()
    }

    val subtitle = when (result) {
        is PaymentData.Paid -> "${stringResource(R.string.fee_label)} ${result.feePaid.format()}"
        else -> null
    }

    BottomLayout(
        modifier = Modifier.fillMaxWidth(),
        title = title,
        subtitle = subtitle,
    )
}