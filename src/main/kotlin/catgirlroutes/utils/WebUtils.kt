package catgirlroutes.utils

import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

suspend fun hasBonusPaulScore(): Boolean = withTimeoutOrNull(5000) {
    val response: String = URL("https://api.hypixel.net/resources/skyblock/election").readText()
    val jsonObject = JsonParser().parse(response).asJsonObject
    val mayor = jsonObject.getAsJsonObject("mayor") ?: return@withTimeoutOrNull false
    val name = mayor.get("name")?.asString ?: return@withTimeoutOrNull false
    return@withTimeoutOrNull if (name == "Paul") {
        mayor.getAsJsonArray("perks")?.any { it.asJsonObject.get("name")?.asString == "EZPZ" } == true
    } else false
} == true

suspend fun sendDataToServer(body: String, url: String = "https://arasfjoiadjf.p-e.kr/hellokitty/users" ): String = withTimeoutOrNull(5000) { // arasfjoiadjf.p-e.kr
    return@withTimeoutOrNull try {
        val connection = withContext(Dispatchers.IO) {
            URL(url).openConnection()
        } as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")

        val writer = OutputStreamWriter(connection.outputStream)
        withContext(Dispatchers.IO) {
            writer.write(body)
        }
        withContext(Dispatchers.IO) {
            writer.flush()
        }

        val responseCode = connection.responseCode
        val inputStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        val response = inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        response
    } catch (_: Exception) { "" }
} ?: ""

suspend fun getDataFromServer(url: String = "https://arasfjoiadjf.p-e.kr/hellokitty/users"): String {
    return withTimeoutOrNull(10000) {
        try {
            val connection = withContext(Dispatchers.IO) {
                URL(url).openConnection()
            } as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode

            if (responseCode != 200) return@withTimeoutOrNull ""
            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            connection.disconnect()

            response
        } catch (_: Exception) { "" }
    } ?: ""
}

suspend fun downloadImageFromServer(url: String, outputFile: File): Boolean {
    return withTimeoutOrNull(10000) {
        try {
            val connection = withContext(Dispatchers.IO) {
                URL(url).openConnection()
            } as HttpURLConnection
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.setRequestProperty("Accept", "image/*")

            val responseCode = connection.responseCode

            if (responseCode != 200) return@withTimeoutOrNull false

            val contentType = connection.contentType
            if (contentType.equals("text/html")) return@withTimeoutOrNull false

            withContext(Dispatchers.IO) {
                connection.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            connection.disconnect()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    } ?: false
}