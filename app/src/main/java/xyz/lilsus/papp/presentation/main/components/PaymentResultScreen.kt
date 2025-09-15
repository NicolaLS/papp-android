package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.presentation.main.PaymentResult
import xyz.lilsus.papp.presentation.mapWalletError
import xyz.lilsus.papp.presentation.model.UiSendPaymentData

@Composable
fun PaymentResultScreen(result: PaymentResult) {
    when (val r = result) {
        is PaymentResult.Error -> {
            BottomLayout(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
                title = "Something went wrong...",
                subtitle = mapWalletError(r.error)
            )
        }

        // TODO: typed amounts and currency formatting (SAT)
        is PaymentResult.Success -> {
            val data = r.data.first
            val title = when (data) {
                UiSendPaymentData.AlreadyPaid -> stringResource(R.string.invoice_already_paid_title)
                UiSendPaymentData.Pending -> stringResource(R.string.payment_pending_title)
                is UiSendPaymentData.Success -> data.amountPaidFormatted
            }

            val subtitle =
                if (data is UiSendPaymentData.Success)
                    "${stringResource(R.string.fee_label)} ${data.feePaidFormatted}" else {
                    null
                }
            BottomLayout(
                title = title,
                subtitle = subtitle,
            )

        }
    }
}