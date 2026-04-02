package net.christianmader.apps.wearos.httpclient.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.christianmader.apps.wearos.httpclient.config.ConfigService
import java.net.URL

data class ConfigUiState(val isConfigLoaded: Boolean = false,
                         val endpointsBaseUrl: String = "",
                         val vibrateOnEndpointSelection: Boolean = false,
                         val showRecentlyUsedEndpoints: Boolean = false,
                         val useXApiKeyHeader: Boolean = false,
                         val xApiKeyHeader: String = "")

class ConfigViewModel(val configService: ConfigService): ViewModel() {
    private val uiStateInternal = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = uiStateInternal

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val endpointsBaseUrlFlow = configService.getEndpointsBaseUrl()
            val vibrateOnEndpointSelectionFlow = configService.vibrateOnEndpointSelection()
            val showRecentlyUsedEndpointsFlow = configService.showRecentlyUsedEndpoints()
            val useXApiKeyHeaderFlow = configService.getUseXApiKeyHeader()
            val xApiKeyHeaderFlow = configService.getXApiKeyHeader()

            combine(endpointsBaseUrlFlow, vibrateOnEndpointSelectionFlow,
                showRecentlyUsedEndpointsFlow, useXApiKeyHeaderFlow, xApiKeyHeaderFlow)
            {
                configValues ->
                ConfigUiState(true,
                    configValues[0] as String,
                    configValues[1] as Boolean,
                    configValues[2] as Boolean,
                    configValues[3] as Boolean,
                    configValues[4] as String)
            }.collect {
                uiStateInternal.value = it }
        }
    }

    fun setEndpointsBaseUrl(url: URL) {
        uiStateInternal.update { it.copy(endpointsBaseUrl = url.toString()) }
    }

    fun setShowRecentlyUsedEndpoints(value: Boolean) {
        uiStateInternal.update { it.copy(showRecentlyUsedEndpoints = value) }
    }

    fun setVibrateOnEndpointSelection(value: Boolean) {
        uiStateInternal.update { it.copy(vibrateOnEndpointSelection = value) }
    }

    fun setUseXApiKeyHeader(value: Boolean) {
        uiStateInternal.update { it.copy(useXApiKeyHeader = value) }
    }

    fun setXApiKeyHeader(value: String) {
        val trimmedValue = value.trim()
        uiStateInternal.update { it.copy(xApiKeyHeader = trimmedValue, useXApiKeyHeader = trimmedValue.isNotEmpty()) }
    }

    fun saveConfig() {
        val endpointsBaseUrl = uiStateInternal.value.endpointsBaseUrl
        val vibrateOnEndpointSelection = uiStateInternal.value.vibrateOnEndpointSelection
        val showRecentlyUsedEndpoints: Boolean = uiStateInternal.value.showRecentlyUsedEndpoints
        val useXApiKeyHeader = uiStateInternal.value.useXApiKeyHeader
        val xApiKeyHeader = uiStateInternal.value.xApiKeyHeader

        var isFieldMissing = false
        viewModelScope.launch(Dispatchers.IO) {
            if (endpointsBaseUrl.isEmpty()) {
                isFieldMissing = true
            }
            else {
                configService.setEndpointsBaseUrl(endpointsBaseUrl)
            }
            configService.setVibrateOnEndpointSelection(vibrateOnEndpointSelection)
            configService.setShowRecentlyUsedEndpoints(showRecentlyUsedEndpoints)
            configService.setUseXApiKeyHeader(useXApiKeyHeader)
            configService.setXApiKeyHeader(xApiKeyHeader)
            configService.setConfigured(!isFieldMissing)
        }
    }
}