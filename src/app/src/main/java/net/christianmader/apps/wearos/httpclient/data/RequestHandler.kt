package net.christianmader.apps.wearos.httpclient.data

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.util.stream.Collectors

class RequestHandler(val endpointsBaseUrl: String, val xApiKey: String = "") {
    data class TextResponse(val text: String = "", val responseCode: Int = HttpURLConnection.HTTP_OK)

    fun retrieveEndpointsDefinition(): TextResponse {
        val endpointsDefinitionUrl = "${endpointsBaseUrl}/endpoints"

        Log.i("EndpointsLoader", "Retrieving endpoints definition from $endpointsDefinitionUrl")
        return sendRequest(endpointsDefinitionUrl, false)
    }

    fun sendRequest(absoluteOrRelativeUrl: String, ignoreException : Boolean = false): TextResponse
    {
        val url: URL = if (absoluteOrRelativeUrl.startsWith("/")) {
            URL(endpointsBaseUrl + absoluteOrRelativeUrl)
        } else {
            URL(absoluteOrRelativeUrl)
        }

        Log.i("EndpointsLoader", "Sending request to $url")
        return performRequest(url, ignoreException, createHeaders(url))
    }

    private fun createHeaders(url: URL): Map<String, String> {
        val headers = HashMap<String, String>()
        listOf(createXApiKeyHeaderLine(),
            createAuthHeaderLine(url)).forEach { pair ->
                pair?.let { pairNotNull -> headers[pairNotNull.first] = pairNotNull.second }
        }
        return headers
    }

    private fun createXApiKeyHeaderLine(): Pair<String, String>? {
        if (xApiKey.isEmpty()) return null
        return Pair("x-api-key", xApiKey)
    }

    private fun createAuthHeaderLine(url: URL): Pair<String, String>? {
        val userInfo = url.userInfo
        if (userInfo != null && userInfo.isNotBlank() && userInfo.contains(':')) {
            val encodedString = Base64.getEncoder().encodeToString(userInfo.encodeToByteArray())
            return Pair("Authorization", "Basic $encodedString")
        }
        return null
    }

    private fun performRequest(endpointsUrl : URL,
                               ignoreException : Boolean,
                               headers : Map<String, String>): TextResponse
    {
        try {
            val urlConnection = endpointsUrl.openConnection() as HttpURLConnection
            val timeoutMillis = 10000
            for (entry in headers.entries) {
                urlConnection.setRequestProperty(entry.key, entry.value)
            }
            urlConnection.connectTimeout = timeoutMillis
            urlConnection.readTimeout = timeoutMillis
            try {
                val contentType = urlConnection.getHeaderField("Content-Type")
                if (contentType != null && contentType.startsWith("text/plain")) {
                    val text = BufferedReader(InputStreamReader(urlConnection.inputStream))
                        .lines()
                        .collect(Collectors.joining("\n"))
                    return TextResponse(text, urlConnection.responseCode)
                }
                else {
                    return TextResponse(responseCode = urlConnection.responseCode)
                }
            }
            finally {
                urlConnection.disconnect()
            }
        }
        catch (e: Exception) {
            if (ignoreException) {
                return TextResponse()
            }
            throw e
        }
    }
}