package net.christianmader.apps.wearos.httpclient.data

import androidx.compose.ui.graphics.Color
import java.util.StringTokenizer

private const val ENDPOINT_PATH_SEPARATOR = "--"
private const val IGNORE_NETWORK_EXCEPTION_INDICATOR = "!"
private const val SPECIAL_PROPERTIES_DELIMITER = "#"

data class EndpointLine(val level: Int,
                        val id : String,
                        val label : String,
                        val url : String,
                        val color: EndpointColor?,
                        val ignoreNetworkException : Boolean = false)

data class EndpointColor(val background: Color, val foreground: Color)

class EndpointsParser(private val endpointsDefinition : String) {
    private var endpointLines : ArrayList<EndpointLine> = ArrayList()

    init {
        createEndpointLines()
    }

    private fun createEndpointLines() {
        val tokenizer = StringTokenizer(endpointsDefinition, "\n")

        while (tokenizer.hasMoreTokens()) {
            val line = tokenizer.nextToken()
            val endpointLine = parseEndpointsDefinitionLine(line)
            endpointLines.add(endpointLine)
        }
    }

    private fun parseEndpointsDefinitionLine(endpointsDefinitionLine : String) : EndpointLine
    {
        val lineFields = StringTokenizer(endpointsDefinitionLine, ",")
        try {
            val hierarchyLevelAndId = lineFields.nextToken()
            val label = lineFields.nextToken()
            val url = if (lineFields.hasMoreTokens()) lineFields.nextToken() else ""

            val level = hierarchyLevelAndId.indexOf(" ")
            if (level == -1) throw IllegalStateException("Could not determine hierarchy level")

            val id = hierarchyLevelAndId.substring(level + 1)
            val idWithoutSpecialProperties = id.split(SPECIAL_PROPERTIES_DELIMITER).first()

            return EndpointLine(level - 1, idWithoutSpecialProperties, label, url, extractColor(id),
                id.endsWith(IGNORE_NETWORK_EXCEPTION_INDICATOR))
        }
        catch (_ : NoSuchElementException) {
            throw IllegalStateException("Hierarchy level/id or label not found")
        }
    }

    private fun extractColor(id: String): EndpointColor? {
        val specialProperties = StringTokenizer(id, SPECIAL_PROPERTIES_DELIMITER)

        if (specialProperties.countTokens() > 1) {
            while (specialProperties.hasMoreTokens()) {
                val token = specialProperties.nextToken()
                val backgroundColor = when (token.replace(IGNORE_NETWORK_EXCEPTION_INDICATOR, "")) {
                    "red" -> Color.Red
                    "green" -> Color.Green
                    "blue" -> Color.Blue
                    "yellow" -> Color.Yellow
                    "cyan" -> Color.Cyan
                    "black" -> Color.Black
                    "magenta" -> Color.Magenta
                    "darkgray" -> Color.DarkGray
                    "lightgray" -> Color.LightGray
                    "gray" -> Color.Gray
                    else -> null
                }
                val foregroundColor = when (backgroundColor) {
                    Color.Green -> Color.Black
                    Color.Yellow -> Color.Black
                    Color.Cyan -> Color.Black
                    Color.LightGray -> Color.Black
                    else -> Color.White
                }

                if (backgroundColor != null) return EndpointColor(backgroundColor, foregroundColor)
            }
        }
        return null
    }

    fun getRoot(): EndpointLine {
        for (endpointLine in endpointLines) {
            if (endpointLine.level == 0) return endpointLine
        }

        throw IllegalStateException("No root endpoint found")
    }

    fun getByPath(path: String): EndpointLine {
        return endpointLines.first { endpointLine -> getPath(endpointLine) == path }
    }

    fun getPath(endpointLine: EndpointLine): String {
        if (endpointLine.level == 0) return endpointLine.id

        val reversedAncestors = ArrayList(getAncestors(endpointLine).reversed().map { it.id }.toList())
        reversedAncestors.add(endpointLine.id)
        return reversedAncestors.joinToString(ENDPOINT_PATH_SEPARATOR)
    }

    fun getAncestors(child: EndpointLine): List<EndpointLine> {
        val ancestors: ArrayList<EndpointLine> = ArrayList()
        val endpointLinesBeforeChild = endpointLines.subList(0, endpointLines.indexOf(child)).reversed()

        var belowLevel = child.level
        for (endpointLine in endpointLinesBeforeChild) {
            if (endpointLine.level < belowLevel) {
                belowLevel = endpointLine.level
                ancestors.add(endpointLine)
            }
        }

        return ancestors
    }

    fun getChildren(parent: EndpointLine): List<EndpointLine> {
        val children: ArrayList<EndpointLine> = ArrayList()
        val endpointLinesAfterParent = endpointLines.subList(endpointLines.indexOf(parent) + 1, endpointLines.size)
        for (endpointLine in endpointLinesAfterParent) {
            if (endpointLine.level == parent.level) break

            if (endpointLine.level == parent.level + 1) {
                children.add(endpointLine)
            }
        }
        return children
    }

    fun inferColor(endpointLine: EndpointLine): EndpointColor? {
        if (endpointLine.color != null) return endpointLine.color

        for (ancestor in getAncestors(endpointLine)) {
            if (ancestor.color != null) return ancestor.color
        }

        return null
    }

}