package xyz.lilsus.papp.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.model.config.wallets.BlinkConfig
import xyz.lilsus.papp.domain.repository.WalletConfigRepository
import xyz.lilsus.papp.domain.repository.WalletKey
import xyz.lilsus.papp.proto.wallet_config.BlinkWalletConfig
import xyz.lilsus.papp.proto.wallet_config.Wallet
import xyz.lilsus.papp.proto.wallet_config.WalletConfig
import xyz.lilsus.papp.proto.wallet_config.WalletConfigStore
import java.security.SecureRandom

class WalletConfigRepositoryImpl(
    private val dataStore: DataStore<WalletConfigStore>
) : WalletConfigRepository {

    override val activeWalletKey =
        dataStore.data.distinctUntilChangedBy { it.activeWalletKey }.map { walletConfigStore ->
            walletConfigStore.activeWalletKey
        }

    override val activeWalletConfigOrNull =
        dataStore.data.distinctUntilChangedBy { it.activeWalletKey }.map { walletConfigStore ->
            val key = walletConfigStore.activeWalletKey
            val wallet = walletConfigStore.walletsMap[key]
            wallet?.let { WalletEntry(it.alias, key, mapConfigProtoToModel(it.config)) }
        }

    override val walletConfigList =
        dataStore.data.map { it.walletsMap }.distinctUntilChanged().map { walletsMap ->
            walletsMap.toList().map {
                val walletConfig = mapConfigProtoToModel(it.second.config)
                WalletEntry(it.second.alias, it.first, walletConfig)
            }
        }

    override suspend fun getWalletConfigOrNull(key: WalletKey): WalletEntry? {
        val walletConfigStore = dataStore.data.first()

        val found = walletConfigStore.walletsMap[key]
        return found?.let { WalletEntry(it.alias, key, mapConfigProtoToModel(it.config)) }
    }


    override suspend fun setActive(key: WalletKey) {
        dataStore.updateData { current ->
            current.toBuilder().setActiveWalletKey(key).build()
        }
    }

    override suspend fun addWallet(
        newWallet: AddWalletEntry,
        activate: Boolean
    ): WalletKey {
        val newKey = generateWalletKey()
        val config = mapConfigModelToProto(newWallet.config)
        val wallet =
            Wallet.newBuilder().setAlias(newWallet.alias).setConfig(config).build()

        dataStore.updateData { current ->
            current.toBuilder()
                .putWallets(
                    newKey,
                    wallet
                )
                .build()
        }

        if (activate) {
            setActive(newKey)
        }

        return newKey
    }

    override suspend fun removeWalletConfig(key: WalletKey) {
        dataStore.updateData { current ->
            val builder = current.toBuilder()

            builder.removeWallets(key)

            if (current.activeWalletKey == key) {
                builder.clearActiveWalletKey()
            }

            builder.build()
        }
    }

    private fun generateWalletKey(): String {
        val bytes = ByteArray(8)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun mapConfigProtoToModel(config: WalletConfig): WalletConfigEntry {
        return when (config.configCase) {
            WalletConfig.ConfigCase.BLINK -> WalletConfigEntry.Blink(
                BlinkConfig(
                    config.blink.apiKey,
                    config.blink.walletId
                )
            )

            WalletConfig.ConfigCase.CONFIG_NOT_SET -> WalletConfigEntry.NotSet
        }
    }

    private fun mapConfigModelToProto(config: WalletConfigEntry): WalletConfig? {
        return when (config) {
            is WalletConfigEntry.Blink -> {
                val blinkConfig = BlinkWalletConfig.newBuilder().setApiKey(config.config.apiKey)
                    .setWalletId(config.config.walletId).build()
                WalletConfig.newBuilder().setBlink(blinkConfig).build()
            }

            WalletConfigEntry.NotSet -> {
                null
            }
        }
    }
}