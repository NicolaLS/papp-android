package xyz.lilsus.papp.domain.model

data class SendPaymentData(
    val amountPaid: Int,
    val feePaid: Int,
    val memo: String?,
)

sealed class SendPaymentResult {
    class Success(val data: SendPaymentData) : SendPaymentResult()
    object AlreadyPaid : SendPaymentResult()
    object Pending : SendPaymentResult()
    class Failure(val message: String) : SendPaymentResult()
}