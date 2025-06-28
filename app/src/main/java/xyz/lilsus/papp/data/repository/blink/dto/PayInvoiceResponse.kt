package xyz.lilsus.papp.data.repository.blink.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentSendResult { ALREADY_PAID, FAILURE, PENDING, SUCCESS, }

@Serializable
enum class TxDirection { RECEIVE, SEND }

@Serializable
enum class TxStatus { FAILURE, PENDING, SUCCESS }

@Serializable
data class Transaction(
    val direction: TxDirection,
    val memo: String? = null,
    val settlementAmount: Int,
    val settlementFee: Int,
    val status: TxStatus
)

@Serializable
@SerialName("error")
data class ErrorDto(
    val code: String? = null,
    val message: String,
    val path: List<String>? = emptyList()
)

@Serializable
data class PaymentSendPayload(
    val errors: List<ErrorDto>? = emptyList(),
    val status: PaymentSendResult? = null,
    val transaction: Transaction? = null,
)

@Serializable
data class PayInvoiceData(
    val lnInvoicePaymentSend: PaymentSendPayload
)

@Serializable
data class PayInvoiceResponse(
    val data: PayInvoiceData
)