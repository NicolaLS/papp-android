package xyz.lilsus.papp.data.api.blink

import android.annotation.SuppressLint
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import xyz.lilsus.papp.data.api.base.ApiError
import xyz.lilsus.papp.data.api.base.ApiResponse
import xyz.lilsus.papp.data.api.base.WalletDataInterpreter
import xyz.lilsus.papp.data.model.WalletError
import xyz.lilsus.papp.data.model.WalletPaymentSendResponse
import xyz.lilsus.papp.data.model.WalletProvider
import xyz.lilsus.papp.data.model.WalletResult

class BlinkWalletDataInterpreter : WalletDataInterpreter {

    override fun handlePayBolt11(res: ApiResponse): WalletResult<WalletPaymentSendResponse> {
        return when (res) {
            is ApiResponse.Failure -> handleFailure(res.error)
            is ApiResponse.Success -> handleSuccess(res.value)
        }
    }

    private fun handleFailure(error: ApiError): WalletResult<WalletPaymentSendResponse> {
        return WalletResult.Failure(WalletError.Client(error))
    }

    @SuppressLint("DefaultLocale")
    private fun handleSuccess(json: JsonElement): WalletResult<WalletPaymentSendResponse> {
        val jsonDecoder = Json { ignoreUnknownKeys = true }
        return try {
            val payload =
                jsonDecoder.decodeFromJsonElement<PayInvoiceResponse>(json).data.lnInvoicePaymentSend
            // FIXME: Blink API Quirk: status might be null instead of FAILURE
            val status = payload.status

            if (status == PaymentSendResult.SUCCESS) {
                // NOTE: Contains negative sign for direction SEND. Remove it here.
                // NOTE: settlementDisplayAmount is the invoice amount plus the fee paid.
                // we remove the fee from this amount because we display it separately.
                if (payload.transaction == null) {
                    return WalletResult.Failure(WalletError.Unexpected("Something went wrong..."))
                }

                val displayAmountPaidTotal = payload.transaction.settlementDisplayAmount
                val displayFeePaid = payload.transaction.settlementDisplayFee

                val displayAmountPaid = displayAmountPaidTotal - displayFeePaid

                return WalletResult.Success(
                    WalletPaymentSendResponse(
                        wallet = WalletProvider.BLINK,
                        displayCurrency = payload.transaction.settlementDisplayCurrency,
                        displayAmountPaid = String.format("%.2f", displayAmountPaid),
                        displayFeePaid = String.format("%.2f", displayFeePaid),
                        memo = payload.transaction.memo,
                    )

                )
            } else if (status == PaymentSendResult.FAILURE) {
                if (payload.errors?.get(0)?.code == "INSUFFICIENT_BALANCE") {
                    return WalletResult.Failure(WalletError.InsufficientBalance)
                }
            }

            // There's also ALREADY_PAID, PENDING and unfortunately even null. We handle all these
            // as errors which might seem weird but we don't have a way of handling them, and they
            // are also specific to blink so we kind of swallow this extra information here.
            return WalletResult.Failure(WalletError.Unexpected("Something went wrong..."))
        } catch (e: Exception) {
            WalletResult.Failure(WalletError.Deserialization(e.message ?: "Invalid format"))
        }
    }
}
