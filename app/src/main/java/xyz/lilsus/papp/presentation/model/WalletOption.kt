package xyz.lilsus.papp.presentation.model

import xyz.lilsus.papp.domain.model.config.WalletEntry


sealed class WalletOption {
    data class Wallet(val entry: WalletEntry) : WalletOption()
    object None : WalletOption()
}
