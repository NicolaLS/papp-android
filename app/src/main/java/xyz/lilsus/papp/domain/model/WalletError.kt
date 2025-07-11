package xyz.lilsus.papp.domain.model

// FIXME: Serializable object must implement 'readResolve'
sealed class WalletError : Throwable() {
    data class PaymentError(override val message: String) : WalletError()
    object MissingStatus : WalletError()
    object MissingTransaction : WalletError()
    data class Client(override val cause: Throwable) : WalletError()
    object Deserialization : WalletError()
}