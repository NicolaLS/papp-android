package xyz.lilsus.papp.data.repository.blink.graphql

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer
import org.junit.Before
import org.junit.Test
import xyz.lilsus.papp.FakeAuthProvider

class OkHttpGraphQLHttpClientTest {
    lateinit var mockHttpClient: OkHttpClient
    lateinit var mockCall: Call
    lateinit var mockResponse: Response
    lateinit var client: OkHttpGraphQLHttpClient

    @Before
    fun setup() {
        val fakeAuthProvider = FakeAuthProvider()
        mockHttpClient = mockk<OkHttpClient>()
        mockCall = mockk<Call>()
        mockResponse = mockk<Response>(relaxed = true)

        every { mockHttpClient.newCall(any()) } returns mockCall
        coEvery { mockCall.execute() } returns mockResponse

        client =
            OkHttpGraphQLHttpClient(fakeAuthProvider, "https://example.com/graphql", mockHttpClient)
    }

    @Test
    fun `post builds Request correctly`() = runBlocking {
        val variables = buildJsonObject {
            putJsonObject("input") {
                put("booleanVariable", true)
                put("stringVariable", "string")
            }
        }

        client.post("mutation", variables)

        // FIXME: Hard code the expected JSON string instead.
        val expectedBody = buildJsonObject {
            put("query", "mutation")
            put("variables", variables)
        }.toString()

        verify {
            mockHttpClient.newCall(match {
                it.url.toString() == "https://example.com/graphql" &&
                        it.header("X-API-KEY") == "KEY" &&
                        it.method == "POST" &&
                        requestBodyToString(it.body) == expectedBody
            })
        }
    }

    @Test
    fun `post returns body when response is valid`() = runBlocking {
        val mockedJson = """{"data":{"result":"ok"}}"""
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body?.string() } returns mockedJson

        val result = client.post("{someQuery}", buildJsonObject { })

        assert(result.isSuccess)
        assert(result.getOrNull() == mockedJson)
    }

    @Test
    fun `post returns error when response body is null`() = runBlocking {
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body?.string() } returns null

        val result = client.post("{someQuery}", buildJsonObject { })
        val error = result.exceptionOrNull()
        assert(error is GraphQLError.EmptyBody)

    }

    @Test
    fun `post returns HttpError when response is unsuccessful`() = runBlocking {
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 500
        every { mockResponse.body?.string() } returns "Server error"

        val result = client.post("{someQuery}", buildJsonObject { })

        assert(result.isFailure)
        val error = result.exceptionOrNull()
        assert(error is GraphQLError.HttpError)
        error as GraphQLError.HttpError
        assert(error.code == 500)
        assert(error.body == "Server error")
    }

    @Test
    fun `post returns NetworkError when exception is thrown`() = runBlocking {
        coEvery { mockCall.execute() } throws java.io.IOException("Network down")

        val result = client.post("{someQuery}", buildJsonObject { })

        assert(result.isFailure)
        val error = result.exceptionOrNull()
        assert(error is GraphQLError.NetworkError)
        val networkException = (error as GraphQLError.NetworkError).cause
        assert(networkException is java.io.IOException)
        assert(networkException.message == "Network down")
    }

    private fun requestBodyToString(body: RequestBody?): String? {
        if (body == null) return null
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readUtf8()
    }
}