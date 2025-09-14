package xyz.lilsus.papp.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xyz.lilsus.papp.R
import xyz.lilsus.papp.domain.model.WalletRepositoryError

// TODO: This could also be ext fun on WalletError
@Composable
fun mapWalletError(walletError: WalletRepositoryError): String {
    return when (walletError) {
        WalletRepositoryError.AuthenticationError -> stringResource(R.string.authentication_error_message)
        WalletRepositoryError.NetworkError -> stringResource(R.string.network_error_message)
        WalletRepositoryError.NoWalletConnected -> stringResource(R.string.no_wallet_connected_message)
        is WalletRepositoryError.ServerError -> stringResource(
            R.string.server_error_message,
            walletError.message ?: "n/a"
        )

        WalletRepositoryError.UnexpectedError -> stringResource(R.string.unexpected_error_message)
        is WalletRepositoryError.WalletError -> stringResource(
            R.string.wallet_error_message,
            walletError.message ?: "n/a"
        )
    }
}