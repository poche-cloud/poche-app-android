package cloud.poche.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SecureStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_storage")

@Singleton
class RealSecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val aead: Aead,
    private val dataStore: DataStore<Preferences> = context.dataStore,
) : SecureStorage {

    override suspend fun putString(key: String, value: String) {
        val encryptedValue = aead.encrypt(value.toByteArray(), null)
        val base64Value = android.util.Base64.encodeToString(encryptedValue, android.util.Base64.DEFAULT)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = base64Value
        }
    }

    override suspend fun getString(key: String): String? {
        val base64Value = dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)]
        }.first() ?: return null

        return try {
            val encryptedValue = android.util.Base64.decode(base64Value, android.util.Base64.DEFAULT)
            val decryptedValue = aead.decrypt(encryptedValue, null)
            String(decryptedValue)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
        }
    }
}
