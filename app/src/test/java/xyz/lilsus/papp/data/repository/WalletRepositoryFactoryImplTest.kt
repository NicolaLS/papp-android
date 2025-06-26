package xyz.lilsus.papp.data.repository

import junit.framework.TestCase
import org.junit.Test
import xyz.lilsus.papp.data.repository.blink.BlinkWalletRepository
import xyz.lilsus.papp.domain.model.config.WalletConfigEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.model.config.wallets.BlinkConfig

class WalletRepositoryFactoryImplTest {
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

        val walletClientFactory = WalletRepositoryFactoryImpl()

        TestCase.assertNull(walletClientFactory.getClientFromConfigOrNull(noWalletConfigured))
        TestCase.assertTrue(walletClientFactory.getClientFromConfigOrNull(blinkWalletConfigured) is BlinkWalletRepository)
    }

}