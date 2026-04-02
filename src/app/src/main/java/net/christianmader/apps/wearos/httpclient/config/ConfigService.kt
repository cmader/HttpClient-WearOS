package net.christianmader.apps.wearos.httpclient.config

import kotlinx.coroutines.flow.Flow
import java.net.URL

interface ConfigService {
    suspend fun isConfigured(): Flow<Boolean>
    suspend fun setConfigured(value: Boolean)

    suspend fun getEndpointsBaseUrl(): Flow<String>
    suspend fun setEndpointsBaseUrl(url: String)

    suspend fun showRecentlyUsedEndpoints(): Flow<Boolean>
    suspend fun setShowRecentlyUsedEndpoints(value: Boolean)
    suspend fun getRecentlyUsedEndpointPaths(): Flow<String>
    suspend fun setRecentlyUsedEndpointPaths(value: String)

    suspend fun getXApiKeyHeader(): Flow<String>
    suspend fun setXApiKeyHeader(value: String)
    suspend fun getUseXApiKeyHeader(): Flow<Boolean>
    suspend fun setUseXApiKeyHeader(value: Boolean)

    suspend fun vibrateOnEndpointSelection(): Flow<Boolean>
    suspend fun setVibrateOnEndpointSelection(value: Boolean)
}