package xyz.lilsus.papp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
}
