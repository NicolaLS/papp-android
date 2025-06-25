package xyz.lilsus.papp.domain.model.config

import xyz.lilsus.papp.domain.model.config.wallets.BlinkConfig

sealed class WalletConfigEntry {
    object NotSet : WalletConfigEntry()
    data class Blink(val config: BlinkConfig) : WalletConfigEntry()
}