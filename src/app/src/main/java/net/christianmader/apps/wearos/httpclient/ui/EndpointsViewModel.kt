package net.christianmader.apps.wearos.httpclient.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.christianmader.apps.wearos.httpclient.R
import net.christianmader.apps.wearos.httpclient.config.ConfigService
import net.christianmader.apps.wearos.httpclient.data.EndpointLine
import net.christianmader.apps.wearos.httpclient.data.EndpointsLoader
import net.christianmader.apps.wearos.httpclient.data.EndpointsParser
import net.christianmader.apps.wearos.httpclient.data.RequestHandler
import java.io.File

private const val MAX_RECENTLY_USED_ENDPOINT_PATHS = 8
private const val RECENTLY_USED_ENDPOINT_PATHS_DELIMITER = ","

data class EndpointsUiState(
    val isRootEndpointLoaded: Boolean = false,
    val currentLevelLabel: String = "",
    val currentLevelPath: String = "",
    val currentLevelIsRoot: Boolean = true,
    val currentLevelIsRecentlyUsedEndpoints: Boolean = false,
    val showRecentlyUsedEndpoints: Boolean = false,
    val recentlyUsedEndpointPaths: List<String> = listOf(),
    val currentLevelChildrenPaths: List<String> = listOf(),
    val busyEndpointPath: String = ""
)

class EndpointsViewModel(
    val configService: ConfigService,
    val localFilesDir: File,
    val forceRetrieveEndpointsDefinitionFromNetwork: Boolean
) : ViewModel() {
    private val uiStateInternal = MutableStateFlow(EndpointsUiState())
    private var endpointsParser: EndpointsParser? = null
    private var requestHandler: RequestHandler? = null
    val userMessageEvents = Channel<UserMessage>()
    val uiState: StateFlow<EndpointsUiState> = uiStateInternal

    init {
        // init recently used endpoints
        viewModelScope.launch(Dispatchers.IO) {
            val showRecentlyUsedEndpointPathsFlow = configService.showRecentlyUsedEndpoints()
            val recentlyUsedEndpointPathsFlow = configService.getRecentlyUsedEndpointPaths()

            combine(
                showRecentlyUsedEndpointPathsFlow, recentlyUsedEndpointPathsFlow
            ) { showRecentlyUsedEndpoints, recentlyUsedEndpointPaths ->
                var recentlyUsedEndpointPathsList = ArrayList(
                    recentlyUsedEndpointPaths
                        .split(RECENTLY_USED_ENDPOINT_PATHS_DELIMITER)
                        .filter { it.isNotEmpty() }
                        .take(MAX_RECENTLY_USED_ENDPOINT_PATHS)
                )

                Pair(showRecentlyUsedEndpoints, recentlyUsedEndpointPathsList)
            }.collect { pair ->
                uiStateInternal.update {
                    it.copy(
                        showRecentlyUsedEndpoints = pair.first,
                        recentlyUsedEndpointPaths = pair.second
                    )
                }
            }
        }

        // load root endpoint
        viewModelScope.launch(Dispatchers.IO) {
            initAndOpenRootEndpoint(forceRetrieveEndpointsDefinitionFromNetwork)
        }
    }

    private data class NetworkSettings(
        val endpointsBaseUrl: String, val xApiKeyHeader: String = ""
    )

    private suspend fun initAndOpenRootEndpoint(forceRetrieveFromNetwork: Boolean = false) {
        val endpointsBaseUrlFlow = configService.getEndpointsBaseUrl()
        val useXApiKeyHeaderFlow = configService.getUseXApiKeyHeader()
        val xApiKeyHeaderFlow = configService.getXApiKeyHeader()

        combine(
            endpointsBaseUrlFlow, useXApiKeyHeaderFlow, xApiKeyHeaderFlow
        ) { endpointsBaseUrl, useXApiKeyHeader, xApiKeyHeader ->
            NetworkSettings(
                endpointsBaseUrl.toString(), if (useXApiKeyHeader) xApiKeyHeader else ""
            )
        }.distinctUntilChanged().collect { networkSettings ->
            openRootEndpoint(networkSettings, forceRetrieveFromNetwork)
        }
    }

    private suspend fun openRootEndpoint(
        networkSettings: NetworkSettings, forceRetrieveFromNetwork: Boolean = false
    ) {
        try {
            requestHandler =
                RequestHandler(networkSettings.endpointsBaseUrl, networkSettings.xApiKeyHeader)
            endpointsParser = createEndpointsParser(forceRetrieveFromNetwork)
            endpointsParser!!.getRoot()
            uiStateInternal.update {
                val rootEndpoint = endpointsParser!!.getRoot()
                val childrenOfRoot = endpointsParser!!.getChildren(rootEndpoint)
                val currentLevelEndpointPaths = childrenOfRoot.map { childEndpointLine ->
                    endpointsParser!!.getPath(childEndpointLine)
                }

                it.copy(
                    isRootEndpointLoaded = true,
                    currentLevelChildrenPaths = currentLevelEndpointPaths,
                    currentLevelIsRoot = true,
                    currentLevelLabel = rootEndpoint.label,
                    currentLevelPath = rootEndpoint.id
                )
            }
        } catch (e: Exception) {
            userMessageEvents.send(
                UserMessage(
                    R.string.endpoints_init_error, e.message ?: "", true, isInitError = true
                )
            )
        }
    }

    private fun createEndpointsParser(forceRetrieveFromNetwork: Boolean = false): EndpointsParser {
        val endpointsLoader = EndpointsLoader(requestHandler!!, localFilesDir)
        val endpointDefinition =
            endpointsLoader.getEndpointsDefinition(forceRetrieveFromNetwork = forceRetrieveFromNetwork)
        return EndpointsParser(endpointDefinition)
    }

    fun openEndpoint(endpointLine: EndpointLine) {
        val parentLabel = endpointLine.label
        val parentPath = endpointsParser!!.getPath(endpointLine)
        val childrenEndpointLines = endpointsParser!!.getChildren(endpointLine)
        val currentLevelEndpointPaths = childrenEndpointLines.map { childEndpointLine ->
            endpointsParser!!.getPath(childEndpointLine)
        }

        uiStateInternal.update {
            it.copy(
                currentLevelLabel = parentLabel,
                currentLevelChildrenPaths = currentLevelEndpointPaths,
                currentLevelPath = parentPath,
                currentLevelIsRecentlyUsedEndpoints = false,
                currentLevelIsRoot = endpointLine.level == 0
            )
        }
    }

    fun openRecentlyUsedEndpoints(parentLabel: String) {
        val recentlyUsedEndpointPaths = uiStateInternal.value.recentlyUsedEndpointPaths

        uiStateInternal.update {
            it.copy(
                currentLevelLabel = parentLabel,
                currentLevelIsRecentlyUsedEndpoints = true,
                currentLevelIsRoot = false,
                currentLevelChildrenPaths = recentlyUsedEndpointPaths
            )
        }
    }

    fun performRequest(endpointLine: EndpointLine, fromRecentlyUsed: Boolean = false) {
        val endpointPath = endpointsParser!!.getPath(endpointLine)
        uiStateInternal.update { it.copy(busyEndpointPath = endpointPath) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.i("EndpointsViewModel", "Performing request to ${endpointLine.url}")
                val textResponse = requestHandler!!.sendRequest(
                    endpointLine.url, endpointLine.ignoreNetworkException
                )
                if (textResponse.responseCode in 200..<300) {
                    Log.i(
                        "EndpointsViewModel",
                        "Request successful (status code: ${textResponse.responseCode})"
                    )
                    if (textResponse.text.isNotEmpty()) {
                        userMessageEvents.send(UserMessage(text = textResponse.text))
                    }
                } else {
                    Log.i(
                        "EndpointsViewModel",
                        "Request failed (status code: ${textResponse.responseCode})"
                    )
                    userMessageEvents.send(
                        UserMessage(
                            R.string.request_error,
                            "Response code: ${textResponse.responseCode}",
                            true
                        )
                    )
                }

                if (!fromRecentlyUsed) {
                    addRecentlyUsedEndpointPath(endpointPath)
                }
            } catch (e: Exception) {
                userMessageEvents.send(UserMessage(R.string.request_error, e.message ?: "", true))
            } finally {
                uiStateInternal.update { it.copy(busyEndpointPath = "") }
            }
        }
    }

    private fun addRecentlyUsedEndpointPath(endpointPath: String) {
        var recentlyUsedEndpointPaths = ArrayList(uiStateInternal.value.recentlyUsedEndpointPaths)

        if (recentlyUsedEndpointPaths.contains(endpointPath)) {
            recentlyUsedEndpointPaths.remove(endpointPath)
        }
        recentlyUsedEndpointPaths.add(0, endpointPath)
        if (recentlyUsedEndpointPaths.size > MAX_RECENTLY_USED_ENDPOINT_PATHS) {
            recentlyUsedEndpointPaths =
                ArrayList(recentlyUsedEndpointPaths.subList(0, MAX_RECENTLY_USED_ENDPOINT_PATHS))
        }

        uiStateInternal.update { it.copy(recentlyUsedEndpointPaths = recentlyUsedEndpointPaths) }
        viewModelScope.launch(Dispatchers.IO) {
            configService.setRecentlyUsedEndpointPaths(
                recentlyUsedEndpointPaths.joinToString(
                    RECENTLY_USED_ENDPOINT_PATHS_DELIMITER
                )
            )
        }
    }

    fun navigateBack() {
        if (uiStateInternal.value.currentLevelIsRecentlyUsedEndpoints) {
            openEndpoint(endpointsParser!!.getRoot())
        } else {
            val currentLevelEndpointLine =
                endpointsParser!!.getByPath(uiStateInternal.value.currentLevelPath)
            val ancestors = endpointsParser!!.getAncestors(currentLevelEndpointLine)
            if (ancestors.isNotEmpty()) {
                openEndpoint(ancestors[0])
            }
        }
    }

    fun getEndpoint(endpointPath: String): EndpointLine {
        return endpointsParser!!.getByPath(endpointPath)
    }

    fun reset() {
        uiStateInternal.update {
            it.copy(
                isRootEndpointLoaded = false, recentlyUsedEndpointPaths = listOf()
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            configService.setRecentlyUsedEndpointPaths("")
            initAndOpenRootEndpoint(true)
        }
    }

    fun getParentLabel(endpointLine: EndpointLine): String {
        return endpointsParser!!.getAncestors(endpointLine).take(1).first().label
    }
}