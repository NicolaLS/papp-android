package xyz.lilsus.papp.data.repository

import xyz.lilsus.papp.data.repository.blink.BlinkAPIKeyAuthProvider
import xyz.lilsus.papp.data.repository.blink.BlinkWalletRepository
import xyz.lilsus.papp.data.repository.blink.graphql.OkHttpGraphQLHttpClient
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.repository.WalletRepositoryFactory

class WalletRepositoryFactoryImpl : WalletRepositoryFactory {
    override fun getClientFromConfigOrNull(walletEntry: WalletEntry): WalletRepository? {
        return when (walletEntry.config) {
            is WalletConfigEntry.Blink -> {
                val config = walletEntry.config.config
                val authProvider = BlinkAPIKeyAuthProvider(config.apiKey)
                val httpClient =
                    OkHttpGraphQLHttpClient(authProvider, BlinkWalletRepository.GRAPHQL_URL)
                BlinkWalletRepository(config.walletId, httpClient)
            }

            WalletConfigEntry.NotSet -> null
        }
    }
}