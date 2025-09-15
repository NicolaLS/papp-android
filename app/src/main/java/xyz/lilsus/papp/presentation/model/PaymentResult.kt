package xyz.lilsus.papp.presentation.model

import androidx.annotation.StringRes
import xyz.lilsus.papp.R
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.SatoshiAmount
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

sealed class PaymentData {
    abstract val walletType: WalletTypeEntry

    // Amounts are Satoshi-based; UI will format using DisplayCurrency.
    data class Paid(
        val amountPaid: SatoshiAmount,
        val feePaid: SatoshiAmount,
        override val walletType: WalletTypeEntry,
    ) : PaymentData()

    data class AlreadyPaid(override val walletType: WalletTypeEntry) : PaymentData()
    data class Pending(override val walletType: WalletTypeEntry) : PaymentData()
}

data class PaymentError(
    @StringRes val titleR: Int,
    @StringRes val messageR: Int,
    val messageStr: String? = null,
    // FIXME: Remove this. Only here to avoid refactor blow up.
    val walletType: WalletTypeEntry
) {
    companion object {
        fun fromDomainWalletError(
            error: WalletRepositoryError,
            walletType: WalletTypeEntry
        ): PaymentError {
            val (title, message) = when (error) {
                WalletRepositoryError.AuthenticationError ->
                    R.string.error_authentication_title to R.string.error_authentication_message

                WalletRepositoryError.NetworkError ->
                    R.string.error_network_title to R.string.error_network_message

                WalletRepositoryError.NoWalletConnected ->
                    R.string.error_no_wallet_title to R.string.error_no_wallet_message

                is WalletRepositoryError.ServerError ->
                    R.string.error_server_title to R.string.error_server_message

                WalletRepositoryError.UnexpectedError ->
                    R.string.error_unexpected_title to R.string.error_unexpected_message

                is WalletRepositoryError.WalletError ->
                    R.string.error_wallet_title to R.string.error_wallet_message
            }
            val messageStr = if (error is WalletRepositoryError.WalletError) error.message else null

            return PaymentError(title, message, messageStr, walletType)
        }
    }
}

