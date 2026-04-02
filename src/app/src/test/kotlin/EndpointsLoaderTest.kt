import net.christianmader.apps.wearos.httpclient.data.EndpointsLoader
import net.christianmader.apps.wearos.httpclient.data.RequestHandler
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.file.Files

class EndpointsLoaderTest {
    val localFilesDir: File = Files.createTempDirectory("localEndpoints").toFile()
    val endpointsLoader = EndpointsLoader(
        requestHandler = RequestHandler("https://hal9001.dedyn.io:4443"),
        localFilesDir = localFilesDir)

    @Test
    fun getFromNetwork() {
        removeLocalEndpointsFile()
        val endpointsDefinition = endpointsLoader.getEndpointsDefinition()

        Assert.assertTrue(endpointsDefinition.isNotEmpty())
    }

    private fun removeLocalEndpointsFile() {
        localFilesDir.listFiles()?.forEach { file ->
            file.deleteRecursively()
        }
    }
}