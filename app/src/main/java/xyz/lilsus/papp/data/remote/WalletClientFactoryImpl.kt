package xyz.lilsus.papp.data.remote

import xyz.lilsus.papp.data.remote.blink.BlinkWalletApi
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.repository.client.WalletApi
import xyz.lilsus.papp.domain.repository.client.WalletClientFactory

class WalletClientFactoryImpl : WalletClientFactory {
    override fun getClientFromConfigOrNull(walletEntry: WalletEntry?): WalletApi? {
        if (walletEntry == null) {
            return null
        }

        return when (walletEntry.config) {
            is WalletConfigEntry.Blink -> BlinkWalletApi(walletEntry.config.config)
            WalletConfigEntry.NotSet -> null
        }
    }
}