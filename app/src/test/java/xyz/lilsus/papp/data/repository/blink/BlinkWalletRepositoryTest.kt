package xyz.lilsus.papp.data.repository.blink

import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.junit.Before
import org.junit.Test
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.data.repository.blink.graphql.GraphQLError
import xyz.lilsus.papp.data.repository.blink.graphql.Mutations
import xyz.lilsus.papp.data.repository.blink.graphql.OkHttpGraphQLHttpClient
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import java.io.IOException

class BlinkWalletRepositoryTest {
    lateinit var mockGraphQLClient: OkHttpGraphQLHttpClient
    lateinit var blinkWalletRepository: BlinkWalletRepository

    companion object {
        private val BOLT11 =
            Bolt11Invoice.parseOrNull("lnbc93440n1p59msy5pp56ft8ayhe7jut3yk2keejpfu66qppkxkwy2nk82ywtlasuxpmen9sdqqcqzzsxqyz5vqsp5eyqt9rq8mv4r3dvzdv007dqmlsvl6fdv2f07yrh74lj7lzg6deqs9qxpqysgqxuf89ejpguvkx5vum5k72j73dfp5gmna93v34qjgd9lsvthfwjqx8qmzt8j8dfscdfxel3ahz8dcksfw4yuwpejmksus5fd2dde7c5cqy9jfc3")!!
        private const val WALLET_ID = "420-69"
    }

    @Before
    fun setup() {
        mockGraphQLClient = mockk<OkHttpGraphQLHttpClient>()
        blinkWalletRepository = BlinkWalletRepository(WALLET_ID, mockGraphQLClient)
    }

    @Test
    fun `payBolt11Invoice calls post on API client with correct query and variables`() {
        coEvery {
            mockGraphQLClient.post(
                Mutations.LnInvoicePaymentSend,
                ofType<JsonObject>(),
            )
        } returns Result.failure(Exception())

        runBlocking {
            blinkWalletRepository.payBolt11Invoice(BOLT11)
        }

        coVerify(exactly = 1) {
            mockGraphQLClient.post(Mutations.LnInvoicePaymentSend, buildJsonObject {
                putJsonObject("input") {
                    put("paymentRequest", BOLT11.encodedSafe)
                    put("walletId", WALLET_ID)
                }
            })
        }
    }

    @Test
    fun `payBolt11Invoice handles GraphQL client EmptyBody error`() {
        coEvery {
            mockGraphQLClient.post(
                any(),
                any()
            )
        } returns Result.failure(GraphQLError.EmptyBody)

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeFailure<WalletRepositoryError.Client>()
        res.exceptionOrNull()!!.cause.shouldBe(GraphQLError.EmptyBody)
    }

    @Test
    fun `payBolt11Invoice handles GraphQL client HttpError error`() {
        coEvery {
            mockGraphQLClient.post(
                any(),
                any()
            )
        } returns Result.failure(GraphQLError.HttpError(500, "Internal Server Error"))

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeFailure<WalletRepositoryError.Client>()
        res.exceptionOrNull()!!.cause.shouldBe(GraphQLError.HttpError(500, "Internal Server Error"))
    }

    @Test
    fun `payBolt11Invoice handles GraphQL client NetworkError error`() {
        coEvery {
            mockGraphQLClient.post(
                any(),
                any()
            )
        } returns Result.failure(GraphQLError.NetworkError(IOException("Timeout")))

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeFailure<WalletRepositoryError.Client>()
        val cause = res.exceptionOrNull()!!.cause
        cause.shouldBeInstanceOf<GraphQLError.NetworkError>()
        cause.cause.shouldBeInstanceOf<IOException>()
        cause.cause.message.shouldBe("Timeout")
    }

    @Test
    fun `payBolt11Invoice handles invalid-JSON string from GraphQL client`() {
        coEvery { mockGraphQLClient.post(any(), any()) } returns Result.success("not even JSON")

        val res = runBlocking {
            blinkWalletRepository.payBolt11Invoice(BOLT11)
        }
        res.shouldBeFailure()
        res.exceptionOrNull().shouldBeInstanceOf<WalletRepositoryError.Deserialization>()
    }

    @Test
    fun `payBolt11Invoice handles API response with missing status`() {
        val jsonResponseStub =
            """{"data":{"lnInvoicePaymentSend":{"errors":[],"status":null,"transaction":null}}}"""
        coEvery { mockGraphQLClient.post(any(), any()) } returns Result.success(jsonResponseStub)

        val res = runBlocking {
            blinkWalletRepository.payBolt11Invoice(BOLT11)
        }
        res.shouldBeFailure()
        res.exceptionOrNull().shouldBeInstanceOf<WalletRepositoryError.MissingStatus>()
    }

    @Test
    fun `payBolt11Invoice handles API SUCCESS response with missing transaction`() {
        val jsonResponseStub =
            """{"data":{"lnInvoicePaymentSend":{"errors":[],"status":"SUCCESS","transaction":null}}}"""
        coEvery { mockGraphQLClient.post(any(), any()) } returns Result.success(jsonResponseStub)

        val res = runBlocking {
            blinkWalletRepository.payBolt11Invoice(BOLT11)
        }
        res.shouldBeFailure()
        res.exceptionOrNull().shouldBeInstanceOf<WalletRepositoryError.MissingTransaction>()
    }

    @Test
    fun `payBolt11Invoice handles valid API SUCCESS response`() {
        val jsonResponseStub =
            """{"data":{"lnInvoicePaymentSend":{"errors":[],"status":"SUCCESS","transaction":{"createdAt":1751313456,"direction":"SEND","id":"6862ec305a85d4a021268c05","memo":null,"settlementAmount":-20,"settlementCurrency":"BTC","settlementDisplayAmount":"-0.02","settlementDisplayCurrency":"USD","settlementDisplayFee":"0.01","settlementFee":10,"status":"SUCCESS"}}}}"""
        coEvery {
            mockGraphQLClient.post(any(), any())
        } returns Result.success(jsonResponseStub)

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeSuccess()
        res.getOrNull().shouldBe(SendPaymentData.Success(10, 10, null))
    }

    @Test
    fun `payBolt11Invoice handles API ALREADY_PAID response`() {
        val jsonResponseStub =
            """{"data":{"lnInvoicePaymentSend":{"errors":[],"status":"ALREADY_PAID","transaction":null}}}"""
        coEvery {
            mockGraphQLClient.post(any(), any())
        } returns Result.success(jsonResponseStub)

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeSuccess()
        res.getOrNull().shouldBe(SendPaymentData.AlreadyPaid)
    }

    @Test
    fun `payBolt11Invoice handles API PENDING response`() {
        val jsonResponseStub =
            """{"data":{"lnInvoicePaymentSend":{"errors":[],"status":"PENDING","transaction":null}}}"""
        coEvery {
            mockGraphQLClient.post(any(), any())
        } returns Result.success(jsonResponseStub)

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeSuccess()
        res.getOrNull().shouldBe(SendPaymentData.Pending)
    }

    @Test
    fun `payBolt11Invoice handles API FAILURE response`() {
        val jsonResponseStub =
            """{"data":{"lnInvoicePaymentSend":{"errors":[{"code":"123","message":"Some error","path":[]}],"status":"FAILURE","transaction":null}}}"""
        coEvery {
            mockGraphQLClient.post(any(), any())
        } returns Result.success(jsonResponseStub)

        val res = runBlocking { blinkWalletRepository.payBolt11Invoice(BOLT11) }
        res.shouldBeFailure<WalletRepositoryError.ApiError>()
        res.exceptionOrNull()!!.message.shouldContain("Error sending payment:")
        res.exceptionOrNull()!!.message.shouldContain("Some error")
    }
}