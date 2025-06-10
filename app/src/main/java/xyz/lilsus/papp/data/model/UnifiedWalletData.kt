package xyz.lilsus.papp.data.model

import xyz.lilsus.papp.data.api.base.ApiError


sealed class WalletError {
    // ApiError pass-through.
    data class Client(val e: ApiError) : WalletError()

    // Deserialization into wallet/`Wallet`ApiResponse fails.
    data class Deserialization(val message: String? = null) : WalletError()

    // Lightning Payment error i.e attempting to pay invoice twice.
    data class LightningPaymentError(val m: String) : WalletError()

    // Connected Wallet has insufficient balance.
    object InsufficientBalance : WalletError()
    data class Unexpected(val m: String) : WalletError()
}

sealed class WalletResult<out T> {
    data class Success<T>(val value: T) : WalletResult<T>()
    data class Failure(val error: WalletError) : WalletResult<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure

    inline fun onSuccess(action: (T) -> Unit): WalletResult<T> {
        if (this is Success) action(value)
        return this
    }

    inline fun onError(action: (WalletError) -> Unit): WalletResult<T> {
        if (this is Failure) action(error)
        return this
    }
}

enum class WalletProvider { BLINK }

// Contains data useful for the UI.
// ConnectedWallets implementation will transform responses from the individual connected wallets.
data class WalletPaymentSendResponse(
    val wallet: WalletProvider,
    // Fiat Currency Code used for amounts below.
    val displayCurrency: String,
    // Paid amount in the `displayCurrency`. Contains the amount of the invoice
    // but not the fee paid. As the recipient "received" this amount even if the
    // user sent more (because of fee)
    val displayAmountPaid: String,
    // Paid fee in the `displayCurrency`.
    val displayFeePaid: String,
    // Optional BOLT11 memo in case there was one.
    val memo: String?,
)