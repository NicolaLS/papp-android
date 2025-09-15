package xyz.lilsus.papp.presentation.model

data class SendPaymentDataDisplayCurrency(
    val amountPaid: DisplayAmount,
    val feePaid: DisplayAmount
)