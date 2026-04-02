package net.christianmader.apps.wearos.httpclient.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.christianmader.apps.wearos.httpclient.config.ConfigService

data class HttpClientAppUiState(val isInitialized: Boolean = false,
                                val isConfigured: Boolean = false)

class HttpClientAppViewModel(val configService: ConfigService): ViewModel() {
    val uiStateInternal = MutableStateFlow(HttpClientAppUiState())
    val uiState: StateFlow<HttpClientAppUiState> = uiStateInternal

    init {
        viewModelScope.launch(Dispatchers.IO) {
            configService.isConfigured().distinctUntilChanged().collect { isConfigured ->
                uiStateInternal.update { it.copy(isInitialized = true, isConfigured = isConfigured) }
            }
        }
    }
}