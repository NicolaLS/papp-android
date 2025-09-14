package xyz.lilsus.papp.data.repository.blink

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.Resource
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

// FIXME: Inject Logger
class BlinkWalletRepository(
    private val walletId: String,
    private val apolloClient: ApolloClient,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : WalletRepository {

    companion object {
        private const val TAG = "BlinkWalletRepository"
    }

    override val walletType = WalletTypeEntry.BLINK

    private suspend fun <D : Operation.Data, R> executeAndTransform(
        apiCall: suspend () -> ApolloResponse<D>,
        onSuccess: (D) -> Resource<R>
    ): Resource<R> {
        val response = withContext(dispatcher) {
            apiCall()
        }
        Log.d(TAG, "Response: $response")

        val exception = response.exception
        if (exception != null) {
            val error = when (exception) {
                is ApolloHttpException -> when (exception.statusCode) {
                    401 -> WalletRepositoryError.AuthenticationError
                    in 500..599 -> WalletRepositoryError.ServerError(exception.message)
                    else -> WalletRepositoryError.UnexpectedError
                }

                is ApolloNetworkException -> WalletRepositoryError.NetworkError
                else -> WalletRepositoryError.UnexpectedError
            }
            Log.e(TAG, "Apollo Exception", exception)
            return Resource.Error(error)
        }

        if (response.hasErrors()) {
            Log.e(TAG, "Unexpected GraphQL error: ${response.errors.toString()}")
            return Resource.Error(WalletRepositoryError.UnexpectedError)
        }

        val data = response.data
        if (data == null) {
            Log.e(TAG, "GraphQL response data was null")
            return Resource.Error(WalletRepositoryError.UnexpectedError)
        }

        return onSuccess(data)
    }

    override suspend fun payBolt11Invoice(bolt11Invoice: Invoice.Bolt11): Resource<SendPaymentData> {
        val mutation = LnInvoicePaymentSendMutation(
            LnInvoicePaymentInput(
                paymentRequest = bolt11Invoice.bolt11.encodedSafe,
                walletId = walletId
            )
        )

        return executeAndTransform(
            apiCall = { apolloClient.mutation(mutation).execute() }
        ) { data ->
            val paymentSend = data.lnInvoicePaymentSend
            when (paymentSend.status) {
                PaymentSendResult.ALREADY_PAID -> Resource.Success(SendPaymentData.AlreadyPaid)
                PaymentSendResult.PENDING -> Resource.Success(SendPaymentData.Pending)
                PaymentSendResult.SUCCESS -> {
                    val tx = paymentSend.transaction
                        ?: return@executeAndTransform Resource.Error(WalletRepositoryError.UnexpectedError)
                    val amountPaidTotal = abs(tx.settlementAmount)
                    val feePaid = abs(tx.settlementFee)
                    Resource.Success(
                        SendPaymentData.Success(
                            amountPaid = amountPaidTotal - feePaid,
                            feePaid = feePaid,
                        )
                    )
                }

                PaymentSendResult.FAILURE -> {
                    val msg = paymentSend.errors.firstOrNull()?.message ?: "Payment failed"
                    Resource.Error(WalletRepositoryError.WalletError(msg))
                }

                PaymentSendResult.UNKNOWN__,
                null -> Resource.Error(WalletRepositoryError.UnexpectedError)
            }
        }
    }

    override suspend fun probeBolt11PaymentFee(bolt11Invoice: Invoice.Bolt11): Resource<Long> {
        val mutation = LnInvoiceFeeProbeMutation(
            LnInvoiceFeeProbeInput(
                paymentRequest = bolt11Invoice.bolt11.encodedSafe,
                walletId = walletId
            )
        )

        return executeAndTransform(
            apiCall = { apolloClient.mutation(mutation).execute() }
        ) { data ->
            val feeProbe = data.lnInvoiceFeeProbe
            val fee = feeProbe.amount ?: return@executeAndTransform Resource.Error(
                WalletRepositoryError.UnexpectedError
            )
            Resource.Success(fee)
        }
    }
}
