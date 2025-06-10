package xyz.lilsus.papp.data.api.base

import xyz.lilsus.papp.data.model.WalletPaymentSendResponse
import xyz.lilsus.papp.data.model.WalletResult

interface WalletDataInterpreter {
    fun handlePayBolt11(res: ApiResponse): WalletResult<WalletPaymentSendResponse>
}