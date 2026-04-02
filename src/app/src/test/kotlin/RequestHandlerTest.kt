import net.christianmader.apps.wearos.httpclient.data.RequestHandler
import org.junit.Assert
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URLConnection

class RequestHandlerTest {
    val requestHandler = RequestHandler("https://hal9001.dedyn.io:4443")

    @Test
    fun retrieveEndpointsDefinition() {
        val endpointsDefinition = requestHandler.retrieveEndpointsDefinition()

        Assert.assertTrue(endpointsDefinition.text.isNotEmpty())
        Assert.assertEquals(HttpURLConnection.HTTP_OK, endpointsDefinition.responseCode)
    }

    @Test
    fun sendRequestRelativeUrl() {
        val textResponse = requestHandler.sendRequest("/endpoints")

        Assert.assertTrue(textResponse.text.isNotEmpty())
        Assert.assertEquals(HttpURLConnection.HTTP_OK, textResponse.responseCode)
    }

    @Test
    fun sendRequestAbsoluteUrl() {
        val textResponse = requestHandler.sendRequest("https://hal9001.dedyn.io:4443/endpoints")

        Assert.assertTrue(textResponse.text.isNotEmpty())
        Assert.assertEquals(HttpURLConnection.HTTP_OK, textResponse.responseCode)
    }

}