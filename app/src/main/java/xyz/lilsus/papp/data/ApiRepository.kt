package xyz.lilsus.papp.data

import xyz.lilsus.papp.util.Bolt11Invoice

// API Repository
// There is going to be a lot of business logic here which will seem like bad architecture, but it
// this is done purposefully as we (will) integrate different connected wallet's and need to deal
// with a lot of their "personal quirks". Hopefully the design can become a bit more elegant later.

// For simplicity I won't define errors for the wallet services. They will throw exceptions, or
// return the response. Keep in mind the 200 OK response from some wallet could be an error
// response itself.

sealed class WalletPaymentSendResult {
    data class Success(val meta: WalletPaymentSendResponse) : WalletPaymentSendResult()
    data class Error(val message: String) : WalletPaymentSendResult()
}

// Contains data useful for the UI.
// ConnectedWallets implementation will transform responses from the individual connected wallets.
data class WalletPaymentSendResponse(
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

interface ApiRepository {
    // Send Lightning Payment to BOLT11 String i.e Pay BOLT11 Invoice.
    // Abstracts LN details as well as individual wallet API details.
    suspend fun payBolt11(bolt11: Bolt11Invoice): WalletPaymentSendResult
}