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
import java.io.IOException

class ApiRepository {

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val GRAPHQL_URL = "https://api.blink.sv/graphql"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()

        private const val PAY_INVOICE_MUTATION = """
            mutation LnInvoicePaymentSend(${'$'}input: LnInvoicePaymentInput!) {
              lnInvoicePaymentSend(input: ${'$'}input) {
                status
                errors {
                  message
                  path
                  code
                }
              }
            }
        """
    }

    suspend fun payInvoice(paymentRequest: String): PayInvoiceResponse =
        withContext(Dispatchers.IO) {
            // Build variables JSON object
            val variables = buildJsonObject {
                putJsonObject("input") {
                    put("paymentRequest", paymentRequest)
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

            json.decodeFromString(PayInvoiceResponse.serializer(), bodyString)
        }
}

@Serializable
data class PayInvoiceResponse(
    val data: PayInvoiceData? = null
)

@Serializable
data class PayInvoiceData(
    val lnInvoicePaymentSend: PaymentSendResult
)

@Serializable
data class PaymentSendResult(
    val status: String,
    val errors: List<PaymentError> = emptyList()
)

@Serializable
data class PaymentError(
    val message: String,
    val path: List<String> = emptyList(),
    val code: String? = null
)
