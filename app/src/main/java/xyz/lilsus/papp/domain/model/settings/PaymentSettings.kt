package xyz.lilsus.papp.domain.model.settings

data class PaymentSettings(
    val alwaysConfirmPayment: Boolean,
    // TODO: Localization / Type Amount (Satoshi for now)
    val confirmPaymentAbove: Float,
)
