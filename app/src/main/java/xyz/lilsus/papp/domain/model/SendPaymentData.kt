package xyz.lilsus.papp.domain.model

import xyz.lilsus.papp.domain.model.config.WalletTypeEntry

data class SendPaymentData(
    val amountPaid: Int,
    val feePaid: Int,
    val memo: String?,
)

sealed class SendPaymentResult {
    class Success(wallet: WalletTypeEntry, val data: SendPaymentData) : SendPaymentResult()
    class AlreadyPaid(wallet: WalletTypeEntry) : SendPaymentResult()
    class Pending(wallet: WalletTypeEntry) : SendPaymentResult()
    class Failure(wallet: WalletTypeEntry, val message: String) : SendPaymentResult()
}