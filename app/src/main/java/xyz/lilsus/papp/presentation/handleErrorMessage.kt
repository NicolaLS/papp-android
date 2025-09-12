package xyz.lilsus.papp.presentation

import xyz.lilsus.papp.domain.model.WalletRepositoryError

// TODO: This could also be ext fun on WalletError
fun handleErrorMessage(walletError: WalletRepositoryError): String {
    // FIXME: Use localized string resources.
    return when (walletError) {
        WalletRepositoryError.AuthenticationError -> "Authentication failed."
        WalletRepositoryError.NetworkError -> "Network error. Please check your internet connection."
        WalletRepositoryError.NoWalletConnected -> "No wallet connected. Connect a wallet in the settings."
        is WalletRepositoryError.ServerError -> "Server error: ${walletError.message}"
        WalletRepositoryError.UnexpectedError -> "Unexpected error. Please try again."
        is WalletRepositoryError.WalletError -> "Wallet error: ${walletError.message}"
    }
}