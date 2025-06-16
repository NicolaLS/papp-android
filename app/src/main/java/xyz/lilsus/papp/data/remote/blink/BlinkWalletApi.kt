package xyz.lilsus.papp.data.remote.blink


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
import xyz.lilsus.papp.domain.model.IntoSendPaymentResult
import xyz.lilsus.papp.domain.repository.WalletApi
import xyz.lilsus.papp.proto.wallet_config.BlinkWalletConfig

// FIXME: https://github.com/NicolaLS/papp-android/issues/11

// Blink GraphQL API doc: https://dev.blink.sv/public-api-reference.html#mutation-lnInvoicePaymentSend
// Don't use GraphQL client because it is not worth it.

class BlinkWalletApi(config: BlinkWalletConfig) : WalletApi {
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

    override suspend fun payBolt11Invoice(invoice: Invoice): IntoSendPaymentResult =
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

                return@withContext data
            }
        }
}