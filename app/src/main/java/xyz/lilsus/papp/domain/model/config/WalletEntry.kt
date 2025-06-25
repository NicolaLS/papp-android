package xyz.lilsus.papp.domain.model.config

data class WalletEntry(
    val alias: String,
    val key: String,
    val config: WalletConfigEntry
)