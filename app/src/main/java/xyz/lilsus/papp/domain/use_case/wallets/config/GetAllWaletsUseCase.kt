package xyz.lilsus.papp.domain.use_case.wallets.config

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.repository.WalletConfigRepository

class GetAllWalletsUseCase(private val repository: WalletConfigRepository) {
    operator fun invoke(): Flow<Resource<List<WalletEntry>>> =
        repository.getAllConfigs()
            .map { Resource.Success(it) as Resource<List<WalletEntry>> }
            .onStart { emit(Resource.Loading()) }
            .catch { emit(Resource.Error(it.message ?: "Unknown Error")) }
}