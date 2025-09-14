package xyz.lilsus.papp.common

import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

typealias WalletResource<D> = Resource<Pair<D, WalletTypeEntry>, WalletRepositoryError>