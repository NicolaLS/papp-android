package xyz.lilsus.papp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.lilsus.papp.util.ApiConstants
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiRepository {

    suspend fun fetchDataFromApi(): String = withContext(Dispatchers.IO) {
        val url = URL("https://jsonplaceholder.typicode.com/todos/1")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                "Error: $responseCode"
            }
        } finally {
            connection.disconnect()
        }
    }

    // TODO: only attempt to pay ln invoices or bitcoin:xxx?ln=xxx
    suspend fun payInvoice(paymentRequest: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.blink.sv/graphql")
        val connection = url.openConnection() as HttpURLConnection

        // The GraphQL mutation query with variables
        val rawJson = """
{
  "query": "mutation LnInvoicePaymentSend(__DOLLAR__input: LnInvoicePaymentInput!) { lnInvoicePaymentSend(input: __DOLLAR__input) { status errors { message path code } } }",
  "variables": {
    "input": {
      "paymentRequest": "$paymentRequest",
      "walletId": "${ApiConstants.WALLET_ID}"
    }
  }
}
""".trimIndent()

        val jsonPayload = rawJson.replace("__DOLLAR__", "$")


        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-API-KEY", ApiConstants.API_KEY)
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Write JSON body
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                "Error: $responseCode - ${connection.errorStream?.bufferedReader()?.readText()}"
            }
        } finally {
            connection.disconnect()
        }
    }
}
