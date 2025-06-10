package xyz.lilsus.papp.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import xyz.lilsus.papp.data.api.alby.AlbyApiClient
import xyz.lilsus.papp.data.api.alby.AlbyWalletDataInterpreter
import xyz.lilsus.papp.data.api.base.WalletClient
import xyz.lilsus.papp.data.api.base.WalletDataInterpreter
import xyz.lilsus.papp.data.api.blink.BlinkApiClient
import xyz.lilsus.papp.data.api.blink.BlinkWalletDataInterpreter
import xyz.lilsus.papp.data.model.WalletPaymentSendResponse
import xyz.lilsus.papp.data.model.WalletResult
import xyz.lilsus.papp.util.Invoice

interface WalletRepository {
    suspend fun payBolt11(invoice: Invoice): List<WalletResult<WalletPaymentSendResponse>>
}

class ConnectedWalletRepository(
    private val clients: List<WalletClient>,
    private val interpreters: List<WalletDataInterpreter>
) : WalletRepository {

    override suspend fun payBolt11(invoice: Invoice): List<WalletResult<WalletPaymentSendResponse>> =
        coroutineScope {
            // Launch async calls for all clients in parallel
            val deferredResults = clients.zip(interpreters).map { (client, interpreter) ->
                async {
                    val res = client.payBolt11(invoice)
                    interpreter.handlePayBolt11(res)
                }
            }

            // Wait for all and return the list of responses
            deferredResults.awaitAll()
        }

    class Builder {
        private val clients = mutableListOf<WalletClient>()
        private val interpreters = mutableListOf<WalletDataInterpreter>()

        fun withBlink() = apply {
            clients.add(BlinkApiClient())
            interpreters.add(BlinkWalletDataInterpreter())
        }

        fun withAlby() = apply {
            clients.add(AlbyApiClient())
            interpreters.add(AlbyWalletDataInterpreter())
        }

        // TODO: Add rates provider to pass to interpreters
        // TODO: Add locale provider to pass to interpreters

        fun build(): ConnectedWalletRepository {
            return ConnectedWalletRepository(clients, interpreters)
        }
    }
}
