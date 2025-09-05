package xyz.lilsus.papp.presentation.ext

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

fun Modifier.tapToDismiss(enabled: Boolean, onDismiss: () -> Unit) = clickable(
    enabled = enabled,
    indication = null,
    interactionSource = null
) { onDismiss() }