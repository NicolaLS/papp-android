package xyz.lilsus.papp.data.remote

import junit.framework.TestCase
import org.junit.Test
import xyz.lilsus.papp.data.remote.blink.BlinkWalletApi
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.model.config.wallets.BlinkConfig

class WalletClientFactoryImplTest {
    @Test
    fun walletClientFactory_returns_correct_client_instance() {
        val noWalletConfigured =
            WalletEntry("Fake Wallet Alias", "0000000000000000", WalletConfigEntry.NotSet)

        val blinkWalletConfigured =
            WalletEntry(
                "Fake Wallet Alias", "0000000000000001", WalletConfigEntry.Blink(
                    BlinkConfig("fake_api_key", "fake_wallet_id")
                )
            )

        val walletClientFactory = WalletClientFactoryImpl()

        TestCase.assertNull(walletClientFactory.getClientFromConfigOrNull(noWalletConfigured))
        TestCase.assertTrue(walletClientFactory.getClientFromConfigOrNull(blinkWalletConfigured) is BlinkWalletApi)
    }

}