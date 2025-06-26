package xyz.lilsus.papp.data.repository.blink


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.model.config.wallets.BlinkConfig
import xyz.lilsus.papp.domain.repository.WalletRepository
import kotlin.math.abs

// FIXME: https://github.com/NicolaLS/papp-android/issues/11

// Blink GraphQL API doc: https://dev.blink.sv/public-api-reference.html#mutation-lnInvoicePaymentSend
// Don't use GraphQL client because it is not worth it.

class BlinkWalletRepository(config: BlinkConfig) : WalletRepository {
    private val client = OkHttpClient()
    val json = Json { ignoreUnknownKeys = true }

    private val walletId = config.walletId
    private val decryptedApiKey = config.apiKey

    companion object {
        private const val GRAPHQL_URL = "https://api.blink.sv/graphql"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()

        private const val PAY_INVOICE_MUTATION = """
	mutation LnInvoicePaymentSend(${'$'}input: LnInvoicePaymentInput!) {
	    lnInvoicePaymentSend(input: ${'$'}input) {
		errors {
		    message
		    path
		    code
		}
		status
		transaction {
		    createdAt
		    direction
		    id
		    memo
		    settlementAmount
		    settlementCurrency
		    settlementDisplayAmount
		    settlementDisplayCurrency
		    settlementDisplayFee
		    settlementFee
		    status
		}

	    }
	}
	"""
    }

    override suspend fun payBolt11Invoice(invoice: Invoice): SendPaymentResult =
        withContext(Dispatchers.IO) {
            val variables = buildJsonObject {
                putJsonObject("input") {
                    put("paymentRequest", invoice.encodedSafe)
                    put("walletId", walletId)
                }
            }

            val requestBodyJson = buildJsonObject {
                put("query", PAY_INVOICE_MUTATION)
                put("variables", variables)
            }.toString()

            val requestBody = requestBodyJson.toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(GRAPHQL_URL)
                .post(requestBody)
                .addHeader("X-API-KEY", decryptedApiKey)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    throw Exception("Unexpected Reply")
                }

                val bodyString = body.string()

                println("BLINK: $bodyString")

                val data = json.decodeFromString(
                    PayInvoiceResponse.serializer(),
                    bodyString
                )

                val wallet = WalletTypeEntry.BLINK
                val payload = data.data.lnInvoicePaymentSend
                val status =
                    payload.status ?: return@withContext SendPaymentResult.Failure(
                        wallet,
                        "Missing payment status"
                    )

                return@withContext when (status) {
                    PaymentSendResult.ALREADY_PAID -> SendPaymentResult.AlreadyPaid(wallet)
                    PaymentSendResult.FAILURE -> {
                        val error =
                            "Error Sending Payment:\n ${payload.errors?.firstOrNull()?.message}"
                        SendPaymentResult.Failure(wallet, error)
                    }

                    PaymentSendResult.PENDING -> SendPaymentResult.Pending(wallet)
                    PaymentSendResult.SUCCESS -> {
                        val tx =
                            payload.transaction ?: return@withContext SendPaymentResult.Failure(
                                wallet,
                                "Missing transaction"
                            )
                        val amountPaidTotal = abs(tx.settlementAmount)
                        val feePaid = abs(tx.settlementFee)
                        val amountPaid = amountPaidTotal - feePaid
                        val memo = tx.memo

                        SendPaymentResult.Success(
                            wallet = wallet,
                            data = SendPaymentData(
                                amountPaid = amountPaid,
                                feePaid = feePaid,
                                memo = memo
                            )
                        )
                    }
                }
            }
        }
}