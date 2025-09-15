package xyz.lilsus.papp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.exchange.ExchangeRate
import xyz.lilsus.papp.domain.model.exchange.ExchangeRateError
import xyz.lilsus.papp.domain.repository.ExchangeRateRepository
import java.util.Currency

class CoinGeckoExchangeRateRepository(
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ExchangeRateRepository {

    override suspend fun getExchangeRate(
        vsCurrency: Currency
    ): Resource<ExchangeRate, ExchangeRateError> = withContext(Dispatchers.IO) {
        val id = "bitcoin"
        val vs = vsCurrency.currencyCode.lowercase()
        val url = "https://api.coingecko.com/api/v3/simple/price?ids=$id&vs_currencies=$vs"
        val request = Request.Builder().url(url).get().build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Resource.Error(ExchangeRateError.Http(response.code))
            }
            val bodyString = response.body?.string()
            if (bodyString.isNullOrBlank()) {
                return@withContext Resource.Error(ExchangeRateError.Parse)
            }
            val root = json.parseToJsonElement(bodyString).jsonObject
            val coin = root[id]?.jsonObject
            val price = coin?.get(vs)?.jsonPrimitive?.content?.toDoubleOrNull()
            if (price == null) {
                return@withContext Resource.Error(ExchangeRateError.Parse)
            }
            Resource.Success(
                ExchangeRate(
                    vsCurrency = vsCurrency,
                    price = price
                )
            )
        } catch (e: Exception) {
            // Differentiate network vs parse if possible
            val isNetwork = e is java.io.IOException
            if (isNetwork) Resource.Error(ExchangeRateError.Network)
            else Resource.Error(ExchangeRateError.Unknown(e.message))
        }
    }
}
