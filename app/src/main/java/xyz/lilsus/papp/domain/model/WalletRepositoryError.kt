package xyz.lilsus.papp.domain.model

sealed interface WalletRepositoryError {
    object UnexpectedError : WalletRepositoryError
    object NoWalletConnected : WalletRepositoryError
    object NetworkError : WalletRepositoryError
    object AuthenticationError : WalletRepositoryError
    data class ServerError(val message: String?) : WalletRepositoryError
    data class WalletError(val message: String?) : WalletRepositoryError
}
