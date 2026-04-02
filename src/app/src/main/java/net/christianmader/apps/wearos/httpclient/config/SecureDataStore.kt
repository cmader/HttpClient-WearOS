package net.christianmader.apps.wearos.httpclient.config

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlin.io.encoding.Base64

val Context.dataStore by preferencesDataStore(name = "secure_settings")

class SecureDataStore(private val context: Context) {
    private val cryptoManager = CryptoManager()

    suspend fun setToken(keyName: String, value: String) {
        val encrypted = cryptoManager.encrypt(value)
        val base64String = Base64.encode(encrypted)

        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(keyName)] = base64String
        }
    }

    fun getToken(keyName: String, defaultValue: String? = null): Flow<String> {
        return context.dataStore.data.mapNotNull { preferences ->
            val base64String = preferences[stringPreferencesKey(keyName)] ?: return@mapNotNull defaultValue
            val encrypted = Base64.decode(base64String)
            val decrypted = cryptoManager.decrypt(encrypted)
            if (decrypted.isEmpty() && defaultValue != null) defaultValue else decrypted
        }

    }
}