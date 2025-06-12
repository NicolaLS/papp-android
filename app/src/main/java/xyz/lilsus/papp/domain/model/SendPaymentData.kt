package xyz.lilsus.papp.domain.model

import xyz.lilsus.papp.common.Wallet

interface IntoSendPaymentResult {
    fun interpretWalletDto(): SendPaymentResult
}

data class SendPaymentData(
    val amountPaid: Int,
    val feePaid: Int,
    val memo: String?,
)

sealed class SendPaymentResult(
    val wallet: Wallet
) {
    class Success(wallet: Wallet, val data: SendPaymentData) : SendPaymentResult(wallet)
    class AlreadyPaid(wallet: Wallet) : SendPaymentResult(wallet)
    class Pending(wallet: Wallet) : SendPaymentResult(wallet)
    class Failure(wallet: Wallet, val message: String) : SendPaymentResult(wallet)
}