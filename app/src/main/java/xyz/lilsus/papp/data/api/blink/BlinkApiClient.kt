package xyz.lilsus.papp.data.api.blink


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import xyz.lilsus.papp.data.api.base.ApiError
import xyz.lilsus.papp.data.api.base.ApiResponse
import xyz.lilsus.papp.data.api.base.WalletClient
import xyz.lilsus.papp.util.ApiConstants
import xyz.lilsus.papp.util.Invoice
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

// FIXME: https://github.com/NicolaLS/papp-android/issues/11

// Blink GraphQL API doc: https://dev.blink.sv/public-api-reference.html#mutation-lnInvoicePaymentSend
// Don't use GraphQL client because it is not worth it.

class BlinkApiClient : WalletClient {
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

    override suspend fun payBolt11(invoice: Invoice): ApiResponse =
        withContext(Dispatchers.IO) {
            // Build variables JSON object
            val variables = buildJsonObject {
                putJsonObject("input") {
                    put("paymentRequest", invoice.encodedSafe)
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
                // Can throw but we don't want to recover from this.
                .url(GRAPHQL_URL)
                .post(requestBody)
                .addHeader("X-API-KEY", ApiConstants.API_KEY)
                .build()

            val response = try {
                client.newCall(request).execute()
            } catch (_: SocketTimeoutException) {
                return@withContext ApiResponse.Failure(ApiError.Timeout)
            } catch (_: UnknownHostException) {
                return@withContext ApiResponse.Failure(ApiError.Network("DNS resolution failed"))
            } catch (_: ConnectException) {
                return@withContext ApiResponse.Failure(ApiError.Network("Connection failed"))
            } catch (_: SSLHandshakeException) {
                return@withContext ApiResponse.Failure(ApiError.Network("SSL handshake failed"))
            } catch (_: InterruptedIOException) {
                return@withContext ApiResponse.Failure(ApiError.Cancelled)
            } catch (e: IOException) {
                return@withContext ApiResponse.Failure(ApiError.Network(e.message ?: "IO error"))
            } catch (e: IllegalStateException) {
                return@withContext ApiResponse.Failure(ApiError.Unexpected(e))
            }

            val body = response.body
            if (!response.isSuccessful || body == null) {
                return@withContext ApiResponse.Failure(
                    ApiError.UnexpectedReply(
                        statusCode = response.code,
                        message = "With Response Body: $body"
                    )
                )
            }

            val jsonElement = try {
                json.parseToJsonElement(body.string())
            } catch (e: SerializationException) {
                response.close()
                return@withContext ApiResponse.Failure(ApiError.Deserialization("Invalid JSON: ${e.message}"))
            }

            // Response/Body needs to be closed to avoid leaks. Body is closed when response is closed.
            response.close()

            return@withContext ApiResponse.Success(jsonElement)
        }
}
