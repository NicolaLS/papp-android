package xyz.lilsus.papp.data

import android.annotation.SuppressLint
import xyz.lilsus.papp.util.Bolt11Invoice

class ConnectedWallets : ApiRepository {
    private val blink = BlinkClient()

    @SuppressLint("DefaultLocale")
    override suspend fun payBolt11(bolt11: Bolt11Invoice): WalletPaymentSendResult {
        // NOTE: Currently assuming wallet = blink btc wallet-id
        try {
            val res = blink.lnInvoicePaymentSend(bolt11.encodedSafe)

            // FIXME: Blink API Quirk: status might be null instead of FAILURE
            val status = res.status

            if (status == PaymentSendResult.SUCCESS) {
                // NOTE: Contains negative sign for direction SEND. Remove it here.
                // NOTE: settlementDisplayAmount is the invoice amount plus the fee paid.
                // we remove the fee from this amount because we display it separately.
                if (res.transaction == null) {
                    return WalletPaymentSendResult.Error("Something went wrong...")
                }

                val displayAmountPaidTotal = res.transaction.settlementDisplayAmount
                val displayFeePaid = res.transaction.settlementDisplayFee

                val displayAmountPaid = displayAmountPaidTotal - displayFeePaid

                return WalletPaymentSendResult.Success(
                    WalletPaymentSendResponse(
                        displayCurrency = res.transaction.settlementDisplayCurrency,
                        displayAmountPaid = String.format("%.2f", displayAmountPaid),
                        displayFeePaid = String.format("%.2f", displayFeePaid),
                        memo = res.transaction.memo,
                    )

                )
            } else if (status == PaymentSendResult.FAILURE) {
                if (res.errors?.get(0)?.code == "INSUFFICIENT_BALANCE") {
                    return WalletPaymentSendResult.Error("Insufficient Balance.\nPlease fund your connected wallet and try again.")
                }
            }

            // There's also ALREADY_PAID, PENDING and unfortunately even null. We handle all these
            // as errors which might seem weird but we don't have a way of handling them, and they
            // are also specific to blink so we kind of swallow this extra information here.
            return WalletPaymentSendResult.Error("Something went wrong...")
        } catch (_: Exception) {
            // TIMEOUT here does NOT imply pending payment, as blink handles that and responds with
            // PENDING (success).
            return WalletPaymentSendResult.Error("Something went wrong...")
        }
    }
}