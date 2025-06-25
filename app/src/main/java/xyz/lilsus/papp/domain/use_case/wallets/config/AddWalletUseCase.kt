package xyz.lilsus.papp.domain.use_case.wallets.config

import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.repository.WalletConfigRepository
import xyz.lilsus.papp.domain.repository.WalletKey

class AddWalletUseCase(private val repository: WalletConfigRepository) {
    suspend operator fun invoke(wallet: AddWalletEntry, activate: Boolean = true): WalletKey {
        val key = repository.addWallet(wallet)
        if (!activate) repository.setActive("")
        return key
    }
}