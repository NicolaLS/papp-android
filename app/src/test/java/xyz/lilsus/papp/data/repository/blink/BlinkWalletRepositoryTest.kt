package xyz.lilsus.papp.data.repository.blink

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestResponse
import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.MockServer
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletRepositoryError
import xyz.lilsus.papp.graphql.LnInvoiceFeeProbeMutation
import xyz.lilsus.papp.graphql.LnInvoicePaymentSendMutation
import xyz.lilsus.papp.graphql.type.LnInvoiceFeeProbeInput
import xyz.lilsus.papp.graphql.type.LnInvoicePaymentInput
import xyz.lilsus.papp.graphql.type.PaymentSendResult

@OptIn(ApolloExperimental::class)
class BlinkWalletRepositoryTest {
    companion object {
        private const val TEST_WALLET_ID = "test-wallet-id"
        private val TEST_INVOICE_SUCCESS =
            Invoice.parse("lnbc93440n1p59msy5pp56ft8ayhe7jut3yk2keejpfu66qppkxkwy2nk82ywtlasuxpmen9sdqqcqzzsxqyz5vqsp5eyqt9rq8mv4r3dvzdv007dqmlsvl6fdv2f07yrh74lj7lzg6deqs9qxpqysgqxuf89ejpguvkx5vum5k72j73dfp5gmna93v34qjgd9lsvthfwjqx8qmzt8j8dfscdfxel3ahz8dcksfw4yuwpejmksus5fd2dde7c5cqy9jfc3") as Invoice.Bolt11
        private val TEST_INVOICE_FAILURE =
            Invoice.parse("lnbc93440n1p59ms9vpp5rnhv4sacupnx0a600qvvzctu9fxkcaj8qzx9wykjwcmjdalur30sdqqcqzzsxqyz5vqsp5xm5rh3v7a24dmhjs57rpqr8qvvywswahkm0kc8s7dqaadg85awvq9qxpqysgqhejddw05xhsyln0d86lyxhkdnd6cmlse3c30cehlm4af2gucm2snwglffvmt0ngstd9kt973sn4cg4uldkz6pgc43cqxy8jgufhvmfgpgvgfda") as Invoice.Bolt11
        private val TEST_INVOICE_ALREADY_PAID =
            Invoice.parse("lnbc93440n1p59ms9upp5d088k9ezuj5rsgx9smj99nq8uj6x6d4h5fapyajhjtt2nc70hz6qdqqcqzzsxqyz5vqsp5vgh965nz33qzjyqt0m92nfntk936lvs9a0dndxjyfkk5m8f4sp3q9qxpqysgqnm5vsrc8k202n2euh45eywte3z5t44zxyx887pf34vrax6sr7duql6aw23znrwxnkudhje7dcpfquda9vekl52edu4zdlmvln5m3qegpkmfjvx") as Invoice.Bolt11
        private val TEST_INVOICE_PENDING =
            Invoice.parse("lnbc93390n1p59msxvpp542spezgwnc0h3unc7qyn9vgfvpx3ugl6gldla8f8zgptptucte0sdqqcqzzsxqyz5vqsp5ve8ymecn3n74k9n2e58rn22rwmnxwrkq5vcr8znf7dt2zlen4dhs9qxpqysgql6qlhwtvvyhsuu27w3wxt67lfewt2khxwkeyyfkqkcr6r65jrytrm395v7r66337cdssexzutpe2qml9wpd59hjvy0vhd9u7tqpkltgpp3a68m") as Invoice.Bolt11
    }

    private lateinit var apolloClient: ApolloClient
    private lateinit var mockServerApolloClient: ApolloClient
    private lateinit var repository: BlinkWalletRepository
    private lateinit var apolloMockServer: MockServer

    private fun useMockServer() {
        repository = BlinkWalletRepository(TEST_WALLET_ID, mockServerApolloClient)
    }

    @Before
    fun setUp() {
        apolloMockServer = MockServer()
        // The apollo client for GraphQL-level mocking
        apolloClient = ApolloClient.Builder()
            .networkTransport(QueueTestNetworkTransport())
            .build()
        // The apollo client for network-level mocking
        runBlocking {
            mockServerApolloClient = ApolloClient.Builder()
                .serverUrl(apolloMockServer.url())
                .build()
        }

        // Default to the queueing client for existing tests
        repository = BlinkWalletRepository(TEST_WALLET_ID, apolloClient)

        // FIXME: Don't depend on Log in data layer
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        apolloMockServer.close()
    }

    @Test
    fun `payBolt11Invoice & probeBolt11PaymentFee - LnInvoicePaymentSendMutation SUCCESS status response - returns correct data`() {
        val testSuccessPayQuery = LnInvoicePaymentSendMutation(
            LnInvoicePaymentInput(
                paymentRequest = TEST_INVOICE_SUCCESS.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )
        val testSuccessFeeQuery = LnInvoiceFeeProbeMutation(
            LnInvoiceFeeProbeInput(
                paymentRequest = TEST_INVOICE_SUCCESS.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )
        val testSuccessPayData = LnInvoicePaymentSendMutation.Data(
            LnInvoicePaymentSendMutation.LnInvoicePaymentSend(
                errors = emptyList(),
                status = PaymentSendResult.SUCCESS,
                transaction = LnInvoicePaymentSendMutation.Transaction(
                    settlementAmount = 21_000L,
                    settlementFee = 21,
                )
            )
        )
        val testSuccessFeeData = LnInvoiceFeeProbeMutation.Data(
            LnInvoiceFeeProbeMutation.LnInvoiceFeeProbe(
                errors = emptyList(),
                amount = 21L
            )
        )

        apolloClient.enqueueTestResponse(testSuccessPayQuery, testSuccessPayData)
        apolloClient.enqueueTestResponse(testSuccessFeeQuery, testSuccessFeeData)

        runBlocking {
            val actualPay = repository.payBolt11Invoice(TEST_INVOICE_SUCCESS.bolt11)
            val actualFee = repository.probeBolt11PaymentFee(TEST_INVOICE_SUCCESS.bolt11)
            actualPay.shouldBeInstanceOf<Resource.Success<*>>()
            actualFee.shouldBeInstanceOf<Resource.Success<*>>()
            (actualPay.data).shouldBeInstanceOf<SendPaymentData.Success>()
            (actualFee.data).shouldBeInstanceOf<Long>()
            // Amount Paid is settlementAmount - settlementFee.
            actualPay.data.amountPaid.shouldBeEqual(21_000L - 21L)
            actualPay.data.feePaid.shouldBeEqual(21L)
            actualFee.data.shouldBeEqual(21L)
        }
    }

    @Test
    fun `payBolt11Invoice - LnInvoicePaymentSendMutation ALREADY_PAID status response - returns correct data`() {
        val testAlreadyPaidQuery = LnInvoicePaymentSendMutation(
            LnInvoicePaymentInput(
                paymentRequest = TEST_INVOICE_ALREADY_PAID.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )

        val testAlreadyPaidDataNoTransaction = LnInvoicePaymentSendMutation.Data(
            LnInvoicePaymentSendMutation.LnInvoicePaymentSend(
                errors = emptyList(),
                status = PaymentSendResult.ALREADY_PAID,
                transaction = null
            )
        )

        val testAlreadyPaidData = testAlreadyPaidDataNoTransaction.copy(
            testAlreadyPaidDataNoTransaction.lnInvoicePaymentSend.copy(transaction = null)
        )

        apolloClient.enqueueTestResponse(testAlreadyPaidQuery, testAlreadyPaidData)
        apolloClient.enqueueTestResponse(testAlreadyPaidQuery, testAlreadyPaidDataNoTransaction)

        runBlocking {
            val actual = repository.payBolt11Invoice(TEST_INVOICE_ALREADY_PAID.bolt11)
            actual.shouldBeInstanceOf<Resource.Success<*>>()
            (actual.data).shouldBeInstanceOf<SendPaymentData.AlreadyPaid>()
        }
    }

    @Test
    fun `payBolt11Invoice - LnInvoicePaymentSendMutation PENDING status response - returns correct data`() {
        val testPendingQuery = LnInvoicePaymentSendMutation(
            LnInvoicePaymentInput(
                paymentRequest = TEST_INVOICE_PENDING.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )
        val testPendingData =
            LnInvoicePaymentSendMutation.Data(
                LnInvoicePaymentSendMutation.LnInvoicePaymentSend(
                    errors = emptyList(),
                    status = PaymentSendResult.PENDING,
                    transaction = null
                )
            )
        apolloClient.enqueueTestResponse(testPendingQuery, testPendingData)

        runBlocking {
            val actual = repository.payBolt11Invoice(TEST_INVOICE_PENDING.bolt11)
            actual.shouldBeInstanceOf<Resource.Success<*>>()
            (actual.data).shouldBeInstanceOf<SendPaymentData.Pending>()
        }
    }

    @Test
    fun `payBolt11Invoice - LnInvoicePaymentSendMutation FAILURE status response - returns correct error`() {
        val testFailureQuery = LnInvoicePaymentSendMutation(
            LnInvoicePaymentInput(
                paymentRequest = TEST_INVOICE_FAILURE.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )
        val testFailureData =
            LnInvoicePaymentSendMutation.Data(
                LnInvoicePaymentSendMutation.LnInvoicePaymentSend(
                    errors = listOf(LnInvoicePaymentSendMutation.Error("Insufficient Balance")),
                    status = PaymentSendResult.FAILURE,
                    transaction = null
                )
            )
        apolloClient.enqueueTestResponse(testFailureQuery, testFailureData)

        runBlocking {
            val actual = repository.payBolt11Invoice(TEST_INVOICE_FAILURE.bolt11)
            actual.shouldBeInstanceOf<Resource.Error>()
            (actual.error).shouldBeInstanceOf<WalletRepositoryError.WalletError>()
            actual.error.message.shouldNotBeNull()
            actual.error.message.shouldBeEqual("Insufficient Balance")
        }
    }

    @Test
    fun `payBolt11Invoice - LnInvoicePaymentSendMutation UNKNOWN__ status response - returns correct error`() {
        val testUnknownQuery = LnInvoicePaymentSendMutation(
            LnInvoicePaymentInput(
                paymentRequest = TEST_INVOICE_FAILURE.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )
        val testUnknownData =
            LnInvoicePaymentSendMutation.Data(
                LnInvoicePaymentSendMutation.LnInvoicePaymentSend(
                    errors = emptyList(),
                    status = PaymentSendResult.UNKNOWN__,
                    transaction = null
                )
            )
        apolloClient.enqueueTestResponse(testUnknownQuery, testUnknownData)

        runBlocking {
            val actual = repository.payBolt11Invoice(TEST_INVOICE_FAILURE.bolt11)
            actual.shouldBeInstanceOf<Resource.Error>()
            (actual.error).shouldBeInstanceOf<WalletRepositoryError.UnexpectedError>()
        }
    }

    @Test
    fun `probeBolt11InvoiceFee - missing amount in response - returns UnexpectedError`() {
        val testSuccessFeeQuery = LnInvoiceFeeProbeMutation(
            LnInvoiceFeeProbeInput(
                paymentRequest = TEST_INVOICE_SUCCESS.bolt11.encodedSafe,
                walletId = TEST_WALLET_ID
            )
        )
        val testSuccessFeeDataNoAmount = LnInvoiceFeeProbeMutation.Data(
            LnInvoiceFeeProbeMutation.LnInvoiceFeeProbe(
                errors = emptyList(),
                amount = null
            )
        )
        apolloClient.enqueueTestResponse(testSuccessFeeQuery, testSuccessFeeDataNoAmount)
        runBlocking {
            val actual = repository.probeBolt11PaymentFee(TEST_INVOICE_SUCCESS.bolt11)
            actual.shouldBeInstanceOf<Resource.Error>()
            (actual.error).shouldBeInstanceOf<WalletRepositoryError.UnexpectedError>()
        }
    }

    @Test
    fun `payBolt11Invoice & probeBolt11InvoiceFee - Http 401 response - returns AuthenticationError`() {
        useMockServer()
        runBlocking {
            // Enqueue two responses, one for each method call
            val mockedHttpResponse = MockResponse.Builder()
                .statusCode(401)
                .body("Unauthorized")
                .delayMillis(500L)
                .build()
            apolloMockServer.enqueue(mockedHttpResponse)
            apolloMockServer.enqueue(mockedHttpResponse)

            val payResult = repository.payBolt11Invoice(TEST_INVOICE_FAILURE.bolt11)
            payResult.shouldBeInstanceOf<Resource.Error>()
            payResult.error.shouldBeInstanceOf<WalletRepositoryError.AuthenticationError>()

            val probeResult = repository.probeBolt11PaymentFee(TEST_INVOICE_FAILURE.bolt11)
            probeResult.shouldBeInstanceOf<Resource.Error>()
            probeResult.error.shouldBeInstanceOf<WalletRepositoryError.AuthenticationError>()
        }
    }

    @Test
    fun `payBolt11Invoice & probeBolt11InvoiceFee - Http 500 response - returns ServerError`() {
        useMockServer()
        runBlocking {
            // Enqueue two responses, one for each method call
            val mockedHttpResponse = MockResponse.Builder()
                .statusCode(500)
                .body("Internal server error")
                .delayMillis(1000L)
                .build()
            apolloMockServer.enqueue(mockedHttpResponse)
            apolloMockServer.enqueue(mockedHttpResponse)

            val payResult = repository.payBolt11Invoice(TEST_INVOICE_FAILURE.bolt11)
            payResult.shouldBeInstanceOf<Resource.Error>()
            payResult.error.shouldBeInstanceOf<WalletRepositoryError.ServerError>()

            val probeResult = repository.probeBolt11PaymentFee(TEST_INVOICE_FAILURE.bolt11)
            probeResult.shouldBeInstanceOf<Resource.Error>()
            probeResult.error.shouldBeInstanceOf<WalletRepositoryError.ServerError>()
        }
    }

    @Test
    fun `payBolt11Invoice & probeBolt11InvoiceFee - Apollo Network Exception - returns NetworkError`() {
        useMockServer()
        runBlocking {
            // Shutting down the server will cause a network exception
            apolloMockServer.close()

            val payResult = repository.payBolt11Invoice(TEST_INVOICE_FAILURE.bolt11)
            payResult.shouldBeInstanceOf<Resource.Error>()
            payResult.error.shouldBeInstanceOf<WalletRepositoryError.NetworkError>()

            val probeResult = repository.probeBolt11PaymentFee(TEST_INVOICE_FAILURE.bolt11)
            probeResult.shouldBeInstanceOf<Resource.Error>()
            probeResult.error.shouldBeInstanceOf<WalletRepositoryError.NetworkError>()
        }
    }
}