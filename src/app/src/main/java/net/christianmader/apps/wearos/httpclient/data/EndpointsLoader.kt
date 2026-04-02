package net.christianmader.apps.wearos.httpclient.data

import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.nio.file.Files

class EndpointsLoader(val requestHandler: RequestHandler, localFilesDir: File)
{
    val localEndpointsFile = File(localFilesDir.path.plus("/endpoints.txt"))

    fun getEndpointsDefinition(forceRetrieveFromNetwork: Boolean = false): String {
        if (!localEndpointsFile.exists() || forceRetrieveFromNetwork) {
            localEndpointsFile.createNewFile()
            Log.i("EndpointsLoader", "Retrieving endpoints definition from network")
            val endpointsDefinition = fetchFromNetwork()
            localEndpointsFile.writeText(endpointsDefinition)
            return endpointsDefinition
        }
        else {
            Log.i("EndpointsLoader", "Using local endpoints definition")
            return Files.readAllLines(localEndpointsFile.toPath()).joinToString("\n")
        }
    }

    private fun fetchFromNetwork(): String {
        val endpointsDef = requestHandler.retrieveEndpointsDefinition()
        if (endpointsDef.text.isNotEmpty() && endpointsDef.responseCode == HttpURLConnection.HTTP_OK) {
            return endpointsDef.text
        }
        else {
            throw NoSuchElementException("Unable to retrieve endpoints definition. " +
                    "Response code: ${endpointsDef.responseCode}")
        }
    }
}