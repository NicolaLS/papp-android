package xyz.lilsus.papp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.lilsus.papp.util.ApiConstants
import xyz.lilsus.papp.util.Invoice
import java.io.IOException

// FIXME: https://github.com/NicolaLS/papp-android/issues/11

// Blink GraphQL API doc: https://dev.blink.sv/public-api-reference.html#mutation-lnInvoicePaymentSend
// Don't use GraphQL client because it is not worth it.

enum class PaymentSendResult {
    ALREADY_PAID,
    FAILURE,
    PENDING,
    SUCCESS,
}

@Serializable
enum class TxDirection { RECEIVE, SEND }

@Serializable
enum class TxStatus { FAILURE, PENDING, SUCCESS }

// TODO: After issue #11 is addressed:
// - Use Bitcoin amounts only
// - Let users configure fiat in the app for the display
@Serializable
data class Transaction(
    val direction: TxDirection,
    val memo: String? = null,
    val settlementDisplayAmount: Float,
    val settlementDisplayCurrency: String,
    val settlementDisplayFee: Float,
    val status: TxStatus
)

@Serializable
data class Error(
    // FIXME: Error code's don't seem to be reliable...
    // only use PaymentSendResult to decide what to do for now.
    val code: String? = null,
    val message: String,
    val path: List<String>? = emptyList()
)

@Serializable
data class PaymentSendPayload(
    val errors: List<Error>? = emptyList(),
    val status: PaymentSendResult? = null,
    val transaction: Transaction? = null,
)

@Serializable
private data class PayInvoiceResponse(
    val data: PayInvoiceData
)

@Serializable
private data class PayInvoiceData(
    val lnInvoicePaymentSend: PaymentSendPayload
)

class BlinkClient {

    private val client = OkHttpClient()
    val json = Json { ignoreUnknownKeys = true }

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

    suspend fun payInvoice(paymentRequest: Invoice): PaymentSendPayload =
        withContext(Dispatchers.IO) {
            // Build variables JSON object
            val variables = buildJsonObject {
                putJsonObject("input") {
                    put("paymentRequest", paymentRequest.encodedSafe)
                    put("walletId", ApiConstants.WALLET_ID)
                }
            }

            // Build full request body JSON
            val requestBodyJson = buildJsonObject {
                put("query", PAY_INVOICE_MUTATION)
                put("variables", variables)
            }.toString()

            val requestBody = requestBodyJson.toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(GRAPHQL_URL)
                .post(requestBody)
                .addHeader("X-API-KEY", ApiConstants.API_KEY)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP code ${response.code}: ${response.message}")
            }

            val bodyString = response.body?.string()
                ?: throw IOException("Empty response body")

            println(bodyString)
            json.decodeFromString(
                PayInvoiceResponse.serializer(),
                bodyString
            ).data.lnInvoicePaymentSend
        }
}
