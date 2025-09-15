package xyz.lilsus.papp.presentation.model

sealed class UiSendPaymentData {
    data class Success(
        val amountPaidFormatted: String,
        val feePaidFormatted: String
    ) : UiSendPaymentData()

    object AlreadyPaid : UiSendPaymentData()
    object Pending : UiSendPaymentData()
}
