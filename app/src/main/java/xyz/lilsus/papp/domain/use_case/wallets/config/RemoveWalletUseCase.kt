package xyz.lilsus.papp.domain.use_case.wallets.config

import xyz.lilsus.papp.domain.repository.WalletConfigRepository
import xyz.lilsus.papp.domain.repository.WalletKey

class RemoveWalletUseCase(private val repository: WalletConfigRepository) {
    suspend operator fun invoke(key: WalletKey) {
        repository.removeWalletConfig(key)
    }
}