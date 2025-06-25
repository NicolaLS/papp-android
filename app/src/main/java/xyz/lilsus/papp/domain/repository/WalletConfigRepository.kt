package xyz.lilsus.papp.domain.repository

import kotlinx.coroutines.flow.Flow
import xyz.lilsus.papp.domain.model.config.AddWalletEntry
import xyz.lilsus.papp.domain.model.config.WalletEntry

typealias WalletKey = String

interface WalletConfigRepository {
    fun getActiveWalletOrNull(): Flow<WalletEntry?>
    fun getAllConfigs(): Flow<List<WalletEntry>>

    suspend fun setActive(key: WalletKey)
    suspend fun addWallet(newWallet: AddWalletEntry, activate: Boolean = true): WalletKey
    suspend fun removeWalletConfig(key: WalletKey)
}