package xyz.lilsus.papp.domain.repository.client

import xyz.lilsus.papp.domain.model.config.WalletEntry

interface WalletClientFactory {
    fun getClientFromConfigOrNull(walletEntry: WalletEntry): WalletApi?
}