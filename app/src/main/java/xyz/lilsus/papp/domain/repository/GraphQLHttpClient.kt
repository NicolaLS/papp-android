package xyz.lilsus.papp.domain.repository

import kotlinx.serialization.json.JsonObject

interface GraphQLHttpClient {
    suspend fun post(query: String, variables: JsonObject): Result<String>
}