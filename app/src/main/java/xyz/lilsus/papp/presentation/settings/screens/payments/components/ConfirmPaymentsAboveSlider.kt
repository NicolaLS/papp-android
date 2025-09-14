package xyz.lilsus.papp.presentation.settings.screens.payments.components

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.lilsus.papp.common.Constants

@Composable
fun ConfirmPaymentsAboveSlider(
    modifier: Modifier = Modifier,
    visible: Boolean,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    if (visible) {
        Slider(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..Constants.DEFAULT_MAX_ABOVE
        )
    }
}