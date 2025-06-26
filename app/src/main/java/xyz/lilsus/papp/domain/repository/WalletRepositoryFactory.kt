package xyz.lilsus.papp.domain.repository

import xyz.lilsus.papp.domain.model.config.WalletEntry

interface WalletRepositoryFactory {
    fun getClientFromConfigOrNull(walletEntry: WalletEntry): WalletRepository?
}