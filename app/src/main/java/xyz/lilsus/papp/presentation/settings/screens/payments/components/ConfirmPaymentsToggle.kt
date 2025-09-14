package xyz.lilsus.papp.presentation.settings.screens.payments.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R

@Composable
fun ConfirmPaymentsToggle(
    modifier: Modifier = Modifier,
    value: Boolean,
    onClick: (Boolean) -> Unit
) {
    val alwaysConfirmColor =
        if (value) MaterialTheme.colorScheme.primary else Color.Gray

    val confirmAboveColor =
        if (!value) MaterialTheme.colorScheme.primary else Color.Gray

    Row(modifier = modifier) {
        TextButton(
            onClick = { onClick(true) },
            colors = ButtonDefaults.textButtonColors(contentColor = alwaysConfirmColor)
        ) { Text(stringResource(R.string.always)) }

        TextButton(
            onClick = { onClick(false) },
            colors = ButtonDefaults.textButtonColors(contentColor = confirmAboveColor)
        ) { Text(stringResource(R.string.above)) }
    }

}