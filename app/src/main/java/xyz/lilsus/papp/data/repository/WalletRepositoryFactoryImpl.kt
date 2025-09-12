package xyz.lilsus.papp.data.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.okHttpClient
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.lilsus.papp.data.repository.blink.BlinkWalletRepository
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.domain.repository.WalletRepositoryFactory

class WalletRepositoryFactoryImpl : WalletRepositoryFactory {

    companion object {
        private const val BLINK_GRAPHQL_URL = "https://api.blink.sv/graphql"
    }

    // FIXME: Clean Architecture here would mean we don't create this here...
    // Could implement another interface e.g BlinkWalletRepositoryProvider which does this
    // then this factory just uses this provider.
    // FIXME: This logs the X-API-KEY header....
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()


    override fun getClientFromConfigOrNull(walletEntry: WalletEntry): WalletRepository? {
        return when (val walletConfig = walletEntry.config) {
            is WalletConfigEntry.Blink -> {
                val config = walletConfig.config
                val apolloClient = ApolloClient.Builder()
                    .serverUrl(BLINK_GRAPHQL_URL)
                    .addHttpHeader("X-API-KEY", config.apiKey)
                    .okHttpClient(okHttpClient)
                    .build()
                BlinkWalletRepository(config.walletId, apolloClient)
            }

            WalletConfigEntry.NotSet -> null
        }
    }
}
