package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.presentation.main.PaymentResult

@Composable
fun PaymentResultScreen(result: PaymentResult) {
    when (val r = result) {
        is PaymentResult.Error -> {
            BottomLayout(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
                title = "Something went wrong...",
                subtitle = r.message
                    ?: "Please try again or check your connection."
            )
        }

        is PaymentResult.Success -> {
            val data = r.data.first
            val title = when (data) {
                SendPaymentData.AlreadyPaid -> "Invoice already paid."
                SendPaymentData.Pending -> "Payment pending."
                is SendPaymentData.Success -> "${data.amountPaid} SAT"
            }

            val subtitle =
                if (data is SendPaymentData.Success)
                    "Fee ${data.feePaid} SAT" else {
                    null
                }
            BottomLayout(
                title = title,
                subtitle = subtitle,
            )

        }
    }
}