package net.christianmader.apps.wearos.httpclient.config

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URL

private const val ENDPOINTS_BASE_URL = "endpoints_base_url"
private const val X_API_KEY = "x_api_key"
private const val USE_X_API_KEY = "use_x_api_key"
private const val IS_CONFIGURED = "is_configured"
private const val SHOW_RECENTLY_USED_ENDPOINTS = "show_recently_used_endpoints"
private const val RECENTLY_USED_ENDPOINT_PATHS = "recently_used_endpoints_paths"
private const val VIBRATE_ON_ENDPOINT_SELECTION = "vibrate_on_endpoint_selection"

class SecurePersistentConfigService(context: Context): ConfigService {
    val dataStore = SecureDataStore(context)

    override suspend fun isConfigured(): Flow<Boolean> {
        return dataStore.getToken(IS_CONFIGURED, "false").map { it == "true" }
    }

    override suspend fun setConfigured(value: Boolean) {
        dataStore.setToken(IS_CONFIGURED, value.toString())
    }

    override suspend fun getEndpointsBaseUrl(): Flow<String> {
        return dataStore.getToken(ENDPOINTS_BASE_URL, "")
    }

    override suspend fun setEndpointsBaseUrl(url: String) {
        dataStore.setToken(ENDPOINTS_BASE_URL, url)
    }

    override suspend fun showRecentlyUsedEndpoints(): Flow<Boolean> {
        return dataStore.getToken(SHOW_RECENTLY_USED_ENDPOINTS, "false").map { it == "true" }
    }

    override suspend fun setShowRecentlyUsedEndpoints(value: Boolean) {
        dataStore.setToken(SHOW_RECENTLY_USED_ENDPOINTS, value.toString())
    }

    override suspend fun getRecentlyUsedEndpointPaths(): Flow<String> {
       return dataStore.getToken(RECENTLY_USED_ENDPOINT_PATHS, "")
    }

    override suspend fun setRecentlyUsedEndpointPaths(value: String) {
        dataStore.setToken(RECENTLY_USED_ENDPOINT_PATHS, value)
    }

    override suspend fun getXApiKeyHeader(): Flow<String> {
        return dataStore.getToken(X_API_KEY, "")
    }

    override suspend fun setXApiKeyHeader(value: String) {
        dataStore.setToken(X_API_KEY, value)
    }

    override suspend fun getUseXApiKeyHeader(): Flow<Boolean> {
        return dataStore.getToken(USE_X_API_KEY, "false").map { it == "true" }
    }

    override suspend fun setUseXApiKeyHeader(value: Boolean) {
        dataStore.setToken(USE_X_API_KEY, value.toString())
    }

    override suspend fun vibrateOnEndpointSelection(): Flow<Boolean> {
        return dataStore.getToken(VIBRATE_ON_ENDPOINT_SELECTION, "false").map { it == "true" }
    }

    override suspend fun setVibrateOnEndpointSelection(value: Boolean) {
        dataStore.setToken(VIBRATE_ON_ENDPOINT_SELECTION, value.toString())
    }
}