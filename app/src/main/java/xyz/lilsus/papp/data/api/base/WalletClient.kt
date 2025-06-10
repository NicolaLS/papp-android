package xyz.lilsus.papp.data.api.base

import kotlinx.serialization.json.JsonElement
import xyz.lilsus.papp.util.Invoice

sealed class ApiError {
    data class Network(val message: String? = null) : ApiError()
    object Timeout : ApiError()
    object Cancelled : ApiError()
    data class Deserialization(val message: String? = null) : ApiError()
    data class Unauthorized(val reason: String? = null) : ApiError()
    data class BadRequest(val details: String? = null) : ApiError()
    data class NotFound(val resource: String? = null) : ApiError()
    data class ServerError(val statusCode: Int, val message: String? = null) : ApiError()
    data class UnexpectedReply(
        val statusCode: Int?,
        val message: String? = null
    ) : ApiError()

    data class Unexpected(val exception: Throwable? = null) : ApiError()
}

sealed class ApiResponse {
    data class Success(val value: JsonElement) : ApiResponse()
    data class Failure(val error: ApiError) : ApiResponse()

    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure

    inline fun onSuccess(action: (JsonElement) -> Unit): ApiResponse {
        if (this is Success) action(value)
        return this
    }

    inline fun onError(action: (ApiError) -> Unit): ApiResponse {
        if (this is Failure) action(error)
        return this
    }
}


interface WalletClient {
    suspend fun payBolt11(invoice: Invoice): ApiResponse
}