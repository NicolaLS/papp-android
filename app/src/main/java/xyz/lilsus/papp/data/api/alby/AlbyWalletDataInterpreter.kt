package xyz.lilsus.papp.data.api.alby

import xyz.lilsus.papp.data.api.base.ApiResponse
import xyz.lilsus.papp.data.api.base.WalletDataInterpreter
import xyz.lilsus.papp.data.model.WalletPaymentSendResponse
import xyz.lilsus.papp.data.model.WalletResult

class AlbyWalletDataInterpreter : WalletDataInterpreter {
    override fun handlePayBolt11(res: ApiResponse): WalletResult<WalletPaymentSendResponse> {
        TODO("Not yet implemented")
    }

}