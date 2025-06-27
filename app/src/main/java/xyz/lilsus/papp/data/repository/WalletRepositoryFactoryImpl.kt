package xyz.lilsus.papp.data.repository

import xyz.lilsus.papp.data.repository.blink.BlinkAPIKeyAuthProvider
import xyz.lilsus.papp.data.repository.blink.BlinkWalletRepository
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.repository.WalletRepositoryFactory

class WalletRepositoryFactoryImpl : WalletRepositoryFactory {
    override fun getClientFromConfigOrNull(walletEntry: WalletEntry): WalletRepository? {
        return when (walletEntry.config) {
            is WalletConfigEntry.Blink -> {
                val authProvider = BlinkAPIKeyAuthProvider(walletEntry.config.config.apiKey)
                BlinkWalletRepository(walletEntry.config.config, authProvider)
            }

            WalletConfigEntry.NotSet -> null
        }
    }
}