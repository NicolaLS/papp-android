package xyz.lilsus.papp.data.api.blink

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
    val settlementDisplayAmount: Float,
    val settlementDisplayCurrency: String,
    val settlementDisplayFee: Float,
    val status: TxStatus
)

@Serializable
data class Error(
    val code: String? = null,
    val message: String,
    val path: List<String>? = emptyList()
)

@Serializable
data class PaymentSendPayload(
    val errors: List<Error>? = emptyList(),
    val status: PaymentSendResult? = null,
    val transaction: Transaction? = null,
)

@Serializable
public data class PayInvoiceResponse(
    val data: PayInvoiceData
)

@Serializable
public data class PayInvoiceData(
    val lnInvoicePaymentSend: PaymentSendPayload
)