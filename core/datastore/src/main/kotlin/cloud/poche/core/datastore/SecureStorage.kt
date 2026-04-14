package cloud.poche.core.datastore

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
interface SecureStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun remove(key: String)
}

@Singleton
class SecureStorageImpl @Inject constructor(private val dataStore: DataStore<Preferences>, private val aead: Aead) :
    SecureStorage {

    override suspend fun putString(key: String, value: String) {
        val encrypted = aead.encrypt(value.toByteArray(Charsets.UTF_8), key.toByteArray(Charsets.UTF_8))
        val base64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = base64
        }
    }

    override suspend fun getString(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        val base64 = dataStore.data.map { it[prefKey] }.first() ?: return null
        return try {
            val encrypted = Base64.decode(base64, Base64.NO_WRAP)
            val decrypted = aead.decrypt(encrypted, key.toByteArray(Charsets.UTF_8))
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }
}
