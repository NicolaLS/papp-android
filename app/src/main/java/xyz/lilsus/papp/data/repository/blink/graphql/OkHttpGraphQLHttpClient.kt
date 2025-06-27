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

class OkHttpGraphQLHttpClient(private val authProvider: AuthProvider, private val url: String) :
    GraphQLHttpClient {
    override suspend fun post(
        query: String,
        variables: JsonObject
    ): String {
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
            OkHttpClient().newCall(request).execute()
        }


        val request = buildRequest()
        return executeRequest(request).use {
            it.body?.string() ?: throw Exception("Unexpected Reply")
        }
    }
}