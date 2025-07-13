package xyz.lilsus.papp.domain.repository

import kotlinx.coroutines.flow.Flow
import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry

typealias WalletKey = String

interface WalletConfigRepository {
    val activeWalletKeyOrNull: Flow<WalletKey?>
    val activeWalletConfigOrNull: Flow<WalletEntry?>
    val walletConfigList: Flow<List<WalletEntry>>

    suspend fun getWalletConfigOrNull(key: WalletKey): WalletEntry?
    suspend fun setActive(key: WalletKey)
    suspend fun addWallet(newWallet: AddWalletEntry, activate: Boolean = true): WalletKey
    suspend fun removeWalletConfig(key: WalletKey)
}