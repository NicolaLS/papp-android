package xyz.lilsus.papp.domain.use_case.wallets.config

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.config.WalletEntry
import xyz.lilsus.papp.domain.repository.WalletConfigRepository

class GetAllWalletsUseCase(private val repository: WalletConfigRepository) {
    operator fun invoke(): Flow<Resource<List<WalletEntry>>> =
        repository.walletConfigList
            .map { Resource.Success(it) as Resource<List<WalletEntry>> }
            .onStart { emit(Resource.Loading) }
}