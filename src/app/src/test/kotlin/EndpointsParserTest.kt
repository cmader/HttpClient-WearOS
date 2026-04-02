import androidx.compose.ui.graphics.Color
import net.christianmader.apps.wearos.httpclient.data.EndpointsParser
import org.junit.Assert
import org.junit.Test

class EndpointsParserTest {
    private fun getFileContent(filename: String): String {
        val inputStream = this.javaClass.getResourceAsStream("/${filename}")
        return inputStream!!.bufferedReader().use { it.readText() }
    }

    @Test
    fun getRoot() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()

        Assert.assertEquals(0, root.level)
        Assert.assertEquals("alles", root.id)
        Assert.assertEquals("Alles", root.label)
        Assert.assertTrue(root.url.isEmpty())
        Assert.assertNull(root.color)
        Assert.assertFalse(root.ignoreNetworkException)
    }

    @Test
    fun getChildrenOfRoot() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()
        val children = endpointsParser.getChildren(root)

        Assert.assertEquals(2, children.size)
        Assert.assertEquals("wohn", children[0].id)
        Assert.assertEquals("test", children[1].id)
    }

    @Test
    fun getChildrenOfFirstLevelEndpoint() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val firstLevelEndpoint = endpointsParser.getByPath("alles--wohn")
        val children = endpointsParser.getChildren(firstLevelEndpoint)

        Assert.assertEquals(2, children.size)
    }

    @Test
    fun getPathOfRoot() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val rootPath = endpointsParser.getPath(endpointsParser.getRoot())

        Assert.assertEquals("alles", rootPath)
    }

    @Test
    fun getPathOfChild() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()
        val children = endpointsParser.getChildren(root)

        val pathFirstChild = endpointsParser.getPath(children[0])
        Assert.assertEquals("alles--wohn", pathFirstChild)
    }

    @Test
    fun getPathOfChildOfChild() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()
        val child = endpointsParser.getChildren(root)[0]
        val childOfChild = endpointsParser.getChildren(child)[0]

        val pathChildOfChild = endpointsParser.getPath(childOfChild)
        Assert.assertEquals("alles--wohn--rollo", pathChildOfChild)
    }

    @Test
    fun getAncestorsOfRoot() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()
        Assert.assertTrue(endpointsParser.getAncestors(root).isEmpty())
    }

    @Test
    fun getAncestors() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()
        val firstChild = endpointsParser.getChildren(root)[0]
        val ancestorsOfChild = endpointsParser.getAncestors(firstChild)

        Assert.assertEquals(1, ancestorsOfChild.size)
        Assert.assertEquals(root, ancestorsOfChild[0])
    }

    @Test
    fun getByPath() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val endpointLine = endpointsParser.getByPath("alles--wohn--musik")

        Assert.assertEquals("musik", endpointLine.id)
    }

    @Test
    fun inferColorOfRoot() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val root = endpointsParser.getRoot()
        Assert.assertNull(endpointsParser.inferColor(root))
    }

    @Test
    fun inferColorOfColoredItem() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val endpointLine = endpointsParser.getByPath("alles--wohn--musik")
        val endpointColor = endpointsParser.inferColor(endpointLine)

        Assert.assertEquals(Color.Green, endpointColor!!.background)
    }

    @Test
    fun inferColorOfIndirectlyColoredItem() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val endpointLine = endpointsParser.getByPath("alles--wohn--musik--m_nr")
        val endpointColor = endpointsParser.inferColor(endpointLine)

        Assert.assertEquals(Color.Green, endpointColor!!.background)
    }

    @Test
    fun invalidHierarchy() {
        try {
            EndpointsParser(getFileContent("endpoints_invalid_hierarchy.txt"))
            Assert.fail()
        }
        catch (_: IllegalStateException) {
        }
    }

    @Test
    fun getEndpointLineWithIgnoreException() {
        val endpointsParser = EndpointsParser(getFileContent("endpoints.txt"))
        val endpointLine = endpointsParser.getByPath("alles--test--tst1")

        Assert.assertTrue(endpointLine.ignoreNetworkException)
    }

}