package xyz.lilsus.papp.data.repository.blink

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.WalletRepository
import xyz.lilsus.papp.graphql.LnInvoiceFeeProbeMutation
import xyz.lilsus.papp.graphql.LnInvoicePaymentSendMutation
import xyz.lilsus.papp.graphql.type.LnInvoiceFeeProbeInput
import xyz.lilsus.papp.graphql.type.LnInvoicePaymentInput
import xyz.lilsus.papp.graphql.type.PaymentSendResult
import kotlin.math.abs

class BlinkWalletRepository(
    private val walletId: String,
    private val apolloClient: ApolloClient,
) : WalletRepository {

    companion object {
        private const val TAG = "BlinkWalletRepository"
    }

    override val walletType = WalletTypeEntry.BLINK

    // Handle fetch errors
    private fun handleApolloException(exception: ApolloException?): WalletRepositoryError? {
        if (exception == null) return null

        Log.e(TAG, "Apollo exception in payBolt11Invoice", exception)
        return when (exception) {
            is ApolloHttpException -> {
                when {
                    exception.statusCode == 401 -> WalletRepositoryError.AuthenticationError
                    exception.statusCode >= 500 -> WalletRepositoryError.ServerError(exception.message)
                    else -> WalletRepositoryError.UnexpectedError
                }
            }

            is ApolloNetworkException -> WalletRepositoryError.NetworkError
            else -> WalletRepositoryError.UnexpectedError
        }
    }

    override suspend fun payBolt11Invoice(bolt11Invoice: Bolt11Invoice): Resource<SendPaymentData> {
        val input = LnInvoicePaymentInput(
            paymentRequest = bolt11Invoice.encodedSafe,
            walletId = walletId
        )
        val mutation = LnInvoicePaymentSendMutation(input)

        val response = apolloClient.mutation(mutation).execute()
        val data = response.data
        Log.d(TAG, "payBolt11Invoice response: $response")

        handleApolloException(response.exception)?.let { return Resource.Error(it) }

        if (response.hasErrors()) {
            // Handle GraphQL errors in response.errors. This should never happen.
            Log.e(TAG, "GraphQL errors in payBolt11Invoice (unexpected): ${response.errors}")
            return Resource.Error(WalletRepositoryError.UnexpectedError)
        }

        // Handle possibly partial data.
        val lnInvoicePaymentSend =
            data?.lnInvoicePaymentSend
                ?: return Resource.Error(WalletRepositoryError.UnexpectedError)

        when (lnInvoicePaymentSend.status) {
            PaymentSendResult.ALREADY_PAID -> {
                return Resource.Success(SendPaymentData.AlreadyPaid)
            }

            PaymentSendResult.PENDING -> {
                return Resource.Success(SendPaymentData.Pending)
            }

            PaymentSendResult.SUCCESS -> {
                val tx = lnInvoicePaymentSend.transaction
                if (tx == null) {
                    Log.e(TAG, "Missing transaction data on successful payment.")
                    return Resource.Error(WalletRepositoryError.UnexpectedError)
                }

                val amountPaidTotal = abs(tx.settlementAmount)
                val feePaid = abs(tx.settlementFee)
                val amountPaid = amountPaidTotal - feePaid

                return Resource.Success(
                    SendPaymentData.Success(
                        amountPaid = amountPaid,
                        feePaid = feePaid,
                    )
                )

            }

            PaymentSendResult.FAILURE -> {
                val errorMessage =
                    lnInvoicePaymentSend.errors.firstOrNull()?.message
                        ?: "Unknown payment error"
                Log.e(TAG, "Payment failure in payBolt11Invoice: $errorMessage")
                return Resource.Error(WalletRepositoryError.WalletError(errorMessage))
            }

            PaymentSendResult.UNKNOWN__,
            null -> {
                return Resource.Error(WalletRepositoryError.UnexpectedError)
            }
        }

    }

    override suspend fun probeBolt11PaymentFee(bolt11Invoice: Bolt11Invoice): Resource<Long> {
        Log.d(TAG, "probeBolt11PaymentFee called with invoice: ${bolt11Invoice.encodedSafe}")
        val input = LnInvoiceFeeProbeInput(
            paymentRequest = bolt11Invoice.encodedSafe,
            walletId = walletId
        )
        val mutation = LnInvoiceFeeProbeMutation(input)

        val response = apolloClient.mutation(mutation).execute()
        val data = response.data
        Log.d(TAG, "probeBolt11PaymentFee response: $response")

        handleApolloException(response.exception)?.let { return Resource.Error(it) }

        if (response.hasErrors()) {
            // Handle GraphQL errors in response.errors. This should never happen.
            Log.e(TAG, "GraphQL errors in payBolt11Invoice (unexpected): ${response.errors}")
            return Resource.Error(WalletRepositoryError.UnexpectedError)
        }

        // Handle possibly partial data.
        val lnInvoiceFeeProbe =
            data?.lnInvoiceFeeProbe
                ?: return Resource.Error(WalletRepositoryError.UnexpectedError)

        val fee =
            lnInvoiceFeeProbe.amount ?: return Resource.Error(WalletRepositoryError.UnexpectedError)
        Log.d(TAG, "probeBolt11PaymentFee successful with (fee) amount: $fee")
        return Resource.Success(fee)
    }
}
