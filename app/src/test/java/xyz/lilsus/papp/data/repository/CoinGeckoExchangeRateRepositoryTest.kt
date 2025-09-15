package xyz.lilsus.papp.data.repository

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import xyz.lilsus.papp.domain.model.Resource
import xyz.lilsus.papp.domain.model.exchange.ExchangeRateError
import java.io.IOException
import java.util.Currency

class CoinGeckoExchangeRateRepositoryTest {

    @MockK(relaxed = true)
    lateinit var client: OkHttpClient

    @MockK
    lateinit var call: Call

    private lateinit var repo: CoinGeckoExchangeRateRepository

    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        repo = CoinGeckoExchangeRateRepository(client = client, json = json)
    }

    private fun buildResponse(code: Int, body: String?): Response {
        val req = Request.Builder().url("https://api.coingecko.com/placeholder").get().build()
        val builder = Response.Builder()
            .request(req)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("")
        if (body != null) {
            builder.body(body.toResponseBody("application/json".toMediaType()))
        }
        return builder.build()
    }

    @Test
    fun `success parses price and uppercases vsCurrency and lowercases query`() = runBlocking {
        val jsonBody = "{" +
                "\"bitcoin\": { \"usd\": 62000.5 }" +
                "}"
        val response = buildResponse(200, jsonBody)

        val capturedRequest = slot<Request>()
        every { client.newCall(capture(capturedRequest)) } returns call
        every { call.execute() } returns response

        val result = repo.getExchangeRate(Currency.getInstance("USD"))

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals("USD", data.vsCurrency.currencyCode)
        assertEquals(62000.5, data.price, 0.0)

        // Verify URL is formed correctly with lowercased vs currency and id=bitcoin
        val url = capturedRequest.captured.url.toString()
        assertEquals(
            "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd",
            url
        )
    }

    @Test
    fun `http error maps to ExchangeRateError_Http`() = runBlocking {
        val response = buildResponse(429, "{ }")
        every { client.newCall(any()) } returns call
        every { call.execute() } returns response

        val result = repo.getExchangeRate(Currency.getInstance("EUR"))

        assertTrue(result is Resource.Error)
        val err = (result as Resource.Error).error
        assertTrue(err is ExchangeRateError.Http)
        assertEquals(429, (err as ExchangeRateError.Http).code)
    }

    @Test
    fun `empty body maps to Parse error`() = runBlocking {
        val response = buildResponse(200, "")
        every { client.newCall(any()) } returns call
        every { call.execute() } returns response

        val result = repo.getExchangeRate(Currency.getInstance("JPY"))

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).error is ExchangeRateError.Parse)
    }

    @Test
    fun `missing fields maps to Parse error`() = runBlocking {
        // Missing the usd field inside bitcoin
        val jsonBody = "{" +
                "\"bitcoin\": { }" +
                "}"
        val response = buildResponse(200, jsonBody)
        every { client.newCall(any()) } returns call
        every { call.execute() } returns response

        val result = repo.getExchangeRate(Currency.getInstance("USD"))

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).error is ExchangeRateError.Parse)
    }

    @Test
    fun `IOException maps to Network error`() = runBlocking {
        every { client.newCall(any()) } returns call
        every { call.execute() } throws IOException("no internet")

        val result = repo.getExchangeRate(Currency.getInstance("USD"))

        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).error is ExchangeRateError.Network)
    }

    @Test
    fun `unexpected exception maps to Unknown error`() = runBlocking {
        every { client.newCall(any()) } returns call
        every { call.execute() } throws RuntimeException("boom")

        val result = repo.getExchangeRate(Currency.getInstance("USD"))

        assertTrue(result is Resource.Error)
        val err = (result as Resource.Error).error
        assertTrue(err is ExchangeRateError.Unknown)
        assertEquals("boom", (err as ExchangeRateError.Unknown).message)
    }
}
