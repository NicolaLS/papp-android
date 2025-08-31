package xyz.lilsus.papp.presentation.settings.screens.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.lilsus.papp.presentation.settings.components.Bar
import xyz.lilsus.papp.presentation.settings.components.Setting
import xyz.lilsus.papp.presentation.settings.screens.payments.components.ConfirmPaymentsAboveSlider
import xyz.lilsus.papp.presentation.settings.screens.payments.components.ConfirmPaymentsToggle

@Composable
fun Payments(
    modifier: Modifier = Modifier,
    viewModel: PaymentsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState

    Scaffold(topBar = { Bar("Payments", onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // TODO: Show loading animation for null value.
            uiState?.let { state ->
                val subtitle =
                    // FIXME: Currency formatting and localization.
                    if (state.alwaysConfirmPayments) "Always" else "Above ${state.confirmPaymentsAbove.toInt()} SAT"

                Setting(
                    title = "Confirm Payment",
                    subtitle = subtitle,
                    contentRight = {
                        ConfirmPaymentsToggle(
                            value = state.alwaysConfirmPayments,
                            onClick = viewModel::setConfirmPaymentAlways
                        )
                    },
                    contentBottom = {
                        ConfirmPaymentsAboveSlider(
                            modifier = Modifier.padding(top = 24.dp),
                            visible = !state.alwaysConfirmPayments,
                            value = state.confirmPaymentsAbove,
                            onValueChange = { viewModel.setConfirmPaymentAbove(it) },
                            onValueChangeFinished = viewModel::save
                        )
                    }
                )
            }
        }
    }
}
