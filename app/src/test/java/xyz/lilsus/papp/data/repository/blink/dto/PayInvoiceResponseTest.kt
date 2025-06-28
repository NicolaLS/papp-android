package xyz.lilsus.papp.data.repository.blink.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletError
import kotlin.math.abs

class PayInvoiceResponseTest {
    @Test
    fun `parse PayInvoiceResponse with missing status correctly`() {
        val responseMissingStatus = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = emptyList(),
                    status = null,
                    transaction = null
                )
            )
        )
        val sendPaymentResult = responseMissingStatus.parse()
        assertTrue(sendPaymentResult.exceptionOrNull() is WalletError.Failure)
        assertEquals("Missing payment status", sendPaymentResult.exceptionOrNull()?.message)
    }

    @Test
    fun `parse PayInvoiceResponse with ALREADY_PAID status correctly`() {
        val responseAlreadyPaidStatus = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = emptyList(),
                    status = PaymentSendResult.ALREADY_PAID,
                    transaction = null
                )
            )
        )
        val sendPaymentResult = responseAlreadyPaidStatus.parse()
        assertTrue(sendPaymentResult.getOrNull() is SendPaymentData.AlreadyPaid)
    }

    @Test
    fun `parse PayInvoiceResponse with PENDING status correctly`() {
        val responsePendingStatus = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = emptyList(),
                    status = PaymentSendResult.PENDING,
                    transaction = null
                )
            )
        )
        val sendPaymentResult = responsePendingStatus.parse()
        assertTrue(sendPaymentResult.getOrNull() is SendPaymentData.Pending)
    }

    @Test
    fun `parse PayInvoiceResponse with FAILURE status without errors correctly`() {
        val responseFailureStatusWithoutErrors = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = emptyList(),
                    status = PaymentSendResult.FAILURE,
                    transaction = null
                )
            )
        )

        var sendPaymentResult = responseFailureStatusWithoutErrors.parse()
        assertTrue(sendPaymentResult.exceptionOrNull() is WalletError.Failure)
        assertTrue(sendPaymentResult.exceptionOrNull()!!.message!!.contains("Error sending payment:"))
    }

    @Test
    fun `parse PayInvoiceResponse with FAILURE status with errors correctly`() {
        val responseFailureStatusWithErrors = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = listOf(
                        xyz.lilsus.papp.data.repository.blink.dto.ErrorDto(
                            "69420",
                            "error message",
                            listOf("path0", "path1")
                        )
                    ),
                    status = PaymentSendResult.FAILURE,
                    transaction = null
                )
            )
        )

        val sendPaymentResult = responseFailureStatusWithErrors.parse()
        assertTrue(sendPaymentResult.exceptionOrNull() is WalletError.Failure)
        assertTrue(sendPaymentResult.exceptionOrNull()!!.message!!.contains("Error sending payment:"))
    }

    @Test
    fun `parse PayInvoiceResponse with SUCCESS status but no transaction correctly`() {
        val responseSuccessStatusNoTransaction = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = emptyList(),
                    status = PaymentSendResult.SUCCESS,
                    transaction = null
                )
            )
        )
        val sendPaymentResult = responseSuccessStatusNoTransaction.parse()
        assertTrue(sendPaymentResult.exceptionOrNull() is WalletError.Failure)
        assertTrue(sendPaymentResult.exceptionOrNull()!!.message!!.contains("Missing transaction"))
    }

    @Test
    fun `parse PayInvoiceResponse with SUCCESS status correctly`() {
        val transaction = Transaction(
            direction = TxDirection.SEND,
            memo = null,
            settlementAmount = 420,
            settlementFee = 69,
            status = TxStatus.SUCCESS
        )
        val responseSuccessStatus = PayInvoiceResponse(
            PayInvoiceData(
                PaymentSendPayload(
                    errors = emptyList(),
                    status = PaymentSendResult.SUCCESS,
                    transaction = transaction
                )
            )
        )
        val sendPaymentResult = responseSuccessStatus.parse()
        assert(sendPaymentResult.getOrNull() is SendPaymentData.Success)
        val data = (sendPaymentResult.getOrNull() as SendPaymentData.Success)
        val amountPaidTotal = abs(transaction.settlementAmount) // 420
        val feePaid = abs(transaction.settlementFee) // 69
        val amountPaid = amountPaidTotal - feePaid // 351

        assertEquals(amountPaid, data.amountPaid)
        assertEquals(feePaid, data.feePaid)
        assertNull(data.memo)
    }
}