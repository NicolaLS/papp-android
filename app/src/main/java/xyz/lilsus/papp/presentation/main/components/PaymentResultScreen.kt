package xyz.lilsus.papp.presentation.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.domain.model.SendPaymentData

@Composable
fun PaymentResultScreen(data: SendPaymentData) {
    when (val result = data) {
        is SendPaymentData.Success -> {
            PaymentSuccessLayout(
                title = "${result.amountPaid} SAT",
                subtitle = "Fee ${result.feePaid} SAT",
                memo = result.memo
            )
        }

        is SendPaymentData.AlreadyPaid -> {
            PaymentSuccessLayout(
                title = "Invoice already paid"
            )
        }

        is SendPaymentData.Pending -> {
            PaymentSuccessLayout(
                title = "Payment is pending..."
            )
        }
    }
}

@Composable
private fun PaymentSuccessLayout(
    title: String,
    subtitle: String? = null,
    memo: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        memo?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
