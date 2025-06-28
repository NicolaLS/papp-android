package xyz.lilsus.papp.domain.model

sealed class SendPaymentData {
    data class Success(val amountPaid: Int, val feePaid: Int, val memo: String?) : SendPaymentData()
    object AlreadyPaid : SendPaymentData()
    object Pending : SendPaymentData()
}