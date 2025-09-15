package xyz.lilsus.papp.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class SettingOption(
    val displayName: String,
    val tag: String
)
