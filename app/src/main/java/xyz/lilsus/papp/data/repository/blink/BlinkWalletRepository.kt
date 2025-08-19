package xyz.lilsus.papp.data.repository.blink


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import xyz.lilsus.papp.common.Bolt11Invoice
import xyz.lilsus.papp.data.repository.blink.dto.PayInvoiceResponse
import xyz.lilsus.papp.data.repository.blink.dto.parse
import xyz.lilsus.papp.data.repository.blink.graphql.Mutations
import xyz.lilsus.papp.domain.model.SendPaymentData
import xyz.lilsus.papp.domain.model.WalletError
import xyz.lilsus.papp.domain.model.config.WalletTypeEntry
import xyz.lilsus.papp.domain.repository.GraphQLHttpClient
import xyz.lilsus.papp.domain.repository.WalletRepository

// FIXME: https://github.com/NicolaLS/papp-android/issues/11

// Blink GraphQL API doc: https://dev.blink.sv/public-api-reference.html#mutation-lnInvoicePaymentSend
// Don't use GraphQL client because it is not worth it.

class BlinkWalletRepository(
    private val walletId: String,
    private val client: GraphQLHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) :
    WalletRepository {

    companion object {
        const val GRAPHQL_URL = "https://api.blink.sv/graphql"
    }

    override val walletType = WalletTypeEntry.BLINK

    override suspend fun payBolt11Invoice(bolt11Invoice: Bolt11Invoice): Result<SendPaymentData> {
        val variables = buildJsonObject {
            putJsonObject("input") {
                put("paymentRequest", bolt11Invoice.encodedSafe)
                put("walletId", walletId)
            }
        }

        val result = client.post(Mutations.LnInvoicePaymentSend, variables)

        return result.fold(
            onSuccess = { bodyString ->
                runCatching { json.decodeFromString(PayInvoiceResponse.serializer(), bodyString) }
                    .mapCatching { it.parse() }
                    .getOrElse { Result.failure(WalletError.Deserialization) }
            },
            onFailure = { error ->
                Result.failure(WalletError.Client(error))
            }
        )
    }
}