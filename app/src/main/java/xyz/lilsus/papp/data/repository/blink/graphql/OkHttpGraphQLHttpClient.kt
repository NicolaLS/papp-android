package xyz.lilsus.papp.data.repository.blink.graphql

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import xyz.lilsus.papp.domain.repository.AuthProvider
import xyz.lilsus.papp.domain.repository.GraphQLHttpClient

class OkHttpGraphQLHttpClient(
    private val authProvider: AuthProvider,
    private val url: String,
    private val client: OkHttpClient = OkHttpClient(),
) :
    GraphQLHttpClient {
    override suspend fun post(
        query: String,
        variables: JsonObject
    ): Result<String> {
        fun buildRequest(): Request {
            val bodyJson = buildJsonObject {
                put("query", query)
                put("variables", variables)
            }.toString()

            val body = bodyJson.toRequestBody("application/json".toMediaType())

            val authHeader = authProvider.getAuthHeader()

            return Request.Builder()
                .url(url)
                .post(body)
                .addHeader(authHeader.first, authHeader.second)
                .build()
        }

        suspend fun executeRequest(request: Request): Response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }


        val request = buildRequest()
        return try {
            executeRequest(request).use {
                if (!it.isSuccessful) {
                    return Result.failure(
                        GraphQLError.HttpError(it.code, it.body?.string())
                    )
                }

                val body = it.body?.string()
                if (body == null) {
                    return Result.failure(GraphQLError.EmptyBody)
                }

                Result.success(body)
            }
        } catch (e: Exception) {
            Result.failure(GraphQLError.NetworkError(e))
        }
    }
}