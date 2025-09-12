package xyz.lilsus.papp.domain.model

sealed class SendPaymentData {
    data class Success(
        // TODO: Add this instead of returning Pair<> from use cases...
        // val walletType: WalletTypeEntry,
        val amountPaid: Long,
        val feePaid: Long
    ) :
        SendPaymentData()

    object AlreadyPaid : SendPaymentData()
    object Pending : SendPaymentData()
}