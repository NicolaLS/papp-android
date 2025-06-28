package xyz.lilsus.papp.domain.model

sealed class WalletError : Throwable() {
    data class Failure(override val message: String) : WalletError()
    data class Client(override val cause: Throwable) : WalletError()
}