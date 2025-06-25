package xyz.lilsus.papp.domain.use_case.wallets

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import xyz.lilsus.papp.common.Invoice
import xyz.lilsus.papp.common.Resource
import xyz.lilsus.papp.domain.model.SendPaymentResult
import xyz.lilsus.papp.domain.repository.WalletRepository

class PayInvoiceUseCase(private val repository: WalletRepository) {
    fun execute(invoice: Invoice): Flow<Resource<SendPaymentResult>> = flow {
        emit(Resource.Loading())
        try {
            val res = repository.payBolt11Invoice(invoice)
            emit(Resource.Success(res))
        } catch (e: Exception) {
            emit(Resource.Error(message = "Something went wrong...\n${e.message}"))
        }

    }
}